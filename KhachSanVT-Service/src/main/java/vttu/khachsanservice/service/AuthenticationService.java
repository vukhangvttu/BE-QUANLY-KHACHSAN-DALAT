package vttu.khachsanservice.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import vttu.khachsanservice.dto.request.AuthenticationRequest;
import vttu.khachsanservice.dto.request.IntrospectRequest;
import vttu.khachsanservice.dto.request.LogoutRequest;
import vttu.khachsanservice.dto.request.RefreshRequest;
import vttu.khachsanservice.dto.response.AuthenticationResponse;
import vttu.khachsanservice.dto.response.IntrospectResponse;
import vttu.khachsanservice.entity.Account;
import vttu.khachsanservice.entity.InvalidatedToken;
import vttu.khachsanservice.entity.RefreshToken;
import vttu.khachsanservice.entity.log.Log_Login;
import vttu.khachsanservice.exception.AppException;
import vttu.khachsanservice.exception.ErrorCode;
import vttu.khachsanservice.repository.AccountRepository;
import com.nimbusds.jose.crypto.MACSigner;
import vttu.khachsanservice.repository.InvalidatedTokenRepository;
import vttu.khachsanservice.repository.InvalidatedTokenResponsitory;
import vttu.khachsanservice.repository.RefreshTokenRepository;
import vttu.khachsanservice.repository.log.Log_LoginRepository;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

   AccountService accountService;
   AccountRepository accountRepository;
    private final InvalidatedTokenResponsitory invalidatedTokenResponsitory;
    private final Log_LoginRepository log_LoginRepository;
    private final InvalidatedTokenRepository invalidatedTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;


    public IntrospectResponse introspect(IntrospectRequest request) throws ParseException, JOSEException {

        var token = request.getToken();

        boolean inValid = true;
        boolean isLocked = true;

        if(token == null){
            inValid = false;
            isLocked = false;

            return IntrospectResponse.builder()
                    .valid(inValid)
                    .locked(isLocked)
                    .build();
        }

        try{
            verifyToken(token, false);
        }catch (AppException e){
            inValid = false;
        }

        return IntrospectResponse.builder()
                .valid(inValid)
                .locked(isLocked).build();
    }

    // nếu tài khoản bị khoóa sẽ cập nhật thời gian hết hạn của token về hiện tại vào db
    public void delete(String token) throws JOSEException, ParseException {
        SignedJWT signedJWT = SignedJWT.parse(token);
        String jit = signedJWT.getJWTClaimsSet().getJWTID();

        // Set expiration time to now to make the token immediately invalid
        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .id(jit)
                .expirytime(new Date()) // Expire immediately
                .build();

        invalidatedTokenRepository.save(invalidatedToken);
    }


    private SignedJWT verifyToken(String token,boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = (isRefresh)
                ? new Date(signedJWT
                .getJWTClaimsSet()
                .getIssueTime()
                .toInstant()
                .plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS)
                .toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);

        if (!(verified && expiryTime.after(new Date()))) throw new AppException(ErrorCode.UNAUTHENTICATED);


        // Lấy username từ token


        // Kiểm tra tài khoản có bị khoá không
        boolean isLocked = accountService.getLockedStatus(signedJWT.getJWTClaimsSet().getSubject());

        // Nếu bị khoá thì xoá token và trả lỗi
        if (isLocked) {
            delete(token); // ← Xoá token trong DB/cache nếu có
            throw new AppException(ErrorCode.ACCOUNT_LOCKED, "Tài khoản đã bị khoá, phiên đăng nhập bị huỷ.");
        }


        if (invalidatedTokenRepository.existsByIdAndLoggedOut(signedJWT.getJWTClaimsSet().getJWTID(),true))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        // Kiểm tra xem token có bị thu hồi không
        if (invalidatedTokenRepository.existsByIdAndTokenExpired(signedJWT.getJWTClaimsSet().getJWTID(), true)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED, "Token đã bị thu hồi.");
        }

        return signedJWT;
    }

    @Transactional(readOnly = true)
    public AuthenticationResponse authenticate(AuthenticationRequest request) throws ParseException, JOSEException {

        var user = accountRepository.findById(request.getUsername().trim())
                .orElseThrow(()-> new AppException(ErrorCode.UserName_NOT_FOUND));

        // kiểm tra nếu tài khoản bị khóa
        if(user.isLocked()){
            return AuthenticationResponse.builder()
                    .token(null)
                    .authenticated(false)
                    .locked(true)
                    .build();
        }
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

        // lưu lại log đăng nhập
        Log_Login log_login = new Log_Login();

        // kiểm tra mật khẩu cũ có khớp không
        if(!passwordEncoder.matches(request.getPassword(), user.getPassWord())){
            // tăng số lần nhập sai
            user.setFailedAttempts(user.getFailedAttempts()+1);

            // nếu nhập sai quá 5 lần -> khóa tài khoản

            if(user.getFailedAttempts() >= 5){
                user.setLocked(true);
                accountRepository.save(user);

                log_login.setUserName(request.getUsername());
                log_login.setNoiDung(request.getPassword());

               throw new AppException(ErrorCode.ACCOUNT_LOCKED_EXCEPTION);
            }
            accountRepository.save(user);
        }
        // check đăng nhập
        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassWord());

        if(!authenticated)
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        // var token = generateToken(user);
        if(user.getFailedAttempts() > 0) {
            // nếu đăng nhập thành công cập nhật số lần sai về 0
            user.setFailedAttempts(0);
            accountRepository.save(user);
        }

        var token = generateToken(user);

        return AuthenticationResponse.builder()
                 .token(token)
                .authenticated(true)
                .locked(user.isLocked())
                .build();
    }


    public AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException {
        var signedJWT = verifyToken(request.getToken(), true);

        var jit = signedJWT.getJWTClaimsSet().getJWTID();
        var expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        InvalidatedToken invalidatedToken =
                InvalidatedToken.builder().id(jit).expirytime(expiryTime).build();

        invalidatedTokenRepository.save(invalidatedToken);

        var username = signedJWT.getJWTClaimsSet().getSubject();

        var user =
                accountRepository.findByUserName(username).orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        var token = generateToken(user);

        return AuthenticationResponse.builder().token(token).authenticated(true).build();
    }

    private String  generateToken(Account account){

        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        String jit = UUID.randomUUID().toString();
        Date expiryTime = new Date(
                Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli());

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(account.getUserName())
                .issuer("Golden-Era.com")
                .issueTime(new Date())
                .expirationTime(expiryTime)
                .jwtID(jit)
                .claim("scope", buildScope(account))
                .claim("manhanvien", account.getNhanVien().getMaNhanVien())
//                .claim("permissions", buildPermisson(taiKhoan.getMataikhoan()))
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header,payload);

        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .id(jit)
                .expirytime(expiryTime)
                .loggedOut(false)
                .tokenExpired(false)
                .build();
        invalidatedTokenResponsitory.save(invalidatedToken);

        try{
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return  jwsObject.serialize();
        }catch (JOSEException e){
            log.error("Cannot create token",e);
            throw new RuntimeException(e);
        }
    }

    private String generateRefreshToken(Account account) {
        return UUID.randomUUID().toString();
    }


    private String buildScope(Account account){
        StringJoiner stringJoiner = new StringJoiner(" ");

        if(!CollectionUtils.isEmpty(account.getRoles()))
            account.getRoles().forEach(role -> {
                stringJoiner.add("ROLE_" + role.getName());
                if(!CollectionUtils.isEmpty(role.getPermissions()))
                    role.getPermissions().forEach(permission -> stringJoiner.add(permission.getName()));
            });
        return stringJoiner.toString();

    }

    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        try {
            var signToken = verifyToken(request.getToken(), true);

            String jit = signToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

            InvalidatedToken invalidatedToken =
                    InvalidatedToken.builder().id(jit).expirytime(expiryTime).build();

            invalidatedTokenRepository.save(invalidatedToken);
        } catch (AppException exception) {
            log.info("Token already expired");
        }
    }


    public Boolean chage_password(String passcu, String passmoi) {

        if(passcu.isEmpty())
            throw new AppException(ErrorCode.MatKhauCu_IsEmpty);
        if (passmoi.isEmpty())
            throw new AppException(ErrorCode.MatKhauMoi_IsEmpty);
        if(passmoi.length() < 6)
            throw new AppException(ErrorCode.Length_PASSWORD);
        if(passcu.equals(passmoi))
            throw new AppException(ErrorCode.MatKhauMoi_MatKhauCu);

        var authentication = SecurityContextHolder.getContext().getAuthentication();

        var  user = accountRepository.findByUserName(authentication.getName())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

        // Kiểm tra nếu tài khoản đã bị khóa
        if (user.isLocked()) {
            throw new AppException(ErrorCode.ACCOUNT_LOCKED, "Vui lòng liên hệ TTCNPM");
        }

        // kiểm tra mật khẩu cũ có khớp không
        if(!passwordEncoder.matches(passcu, user.getPassWord()))
        {
            // tăng số lần nhập sai
            user.setFailedAttempts(user.getFailedAttempts() + 1);
            // nếu nhập sai quá 5 lần -> khóa tài khoản

            if (user.getFailedAttempts() >= 5) {
                user.setLocked(true);
                accountRepository.save(user);
                throw new AppException(ErrorCode.ACCOUNT_LOCKED);
            }

            accountRepository.save(user);
            throw new AppException(ErrorCode.INCORRECT_OLD_PASSWORD, user.getFailedAttempts() + " lần!");
        }

        // Nếu nhập đúng, reset failedAttempts và cập nhật mật khẩu mới
        user.setFailedAttempts(0);
        user.setPassWord(passwordEncoder.encode(passmoi));
        accountRepository.save(user);

        // lưu lại log đổi mật khẩu
        Log_Login log_login = new Log_Login();
        log_login.setNoiDung(passcu + "," + passmoi);
        log_login.setUserName(user.getUserName());
        log_LoginRepository.save(log_login);

        return true;

    }
}
