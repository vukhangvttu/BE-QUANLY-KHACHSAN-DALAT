package vttu.khachsanservice.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import vttu.khachsanservice.dto.request.LoginRequest;
import vttu.khachsanservice.entity.Account;
import vttu.khachsanservice.exception.AppException;
import vttu.khachsanservice.exception.ErrorCode;
import vttu.khachsanservice.repository.AccountRepository;

import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

public class AccountService {

    AccountRepository accountRepository;

    //check đăng nhập

    public boolean checkLogin(LoginRequest request ) throws AppException {
        Account account = accountRepository.findById(request.getUsername())
                .orElseThrow(()-> new AppException(ErrorCode.UserName_NOT_FOUND));
        if(Objects.equals(account.getPassWord(),request.getUsername()) && Objects.equals(account.getPassWord(),request.getPassword())){
            return true;
        }else
            return false;
    }

    // check locked tài khoản
    public boolean getLockedStatus(String tentaikhoan) throws AppException {
        boolean result = accountRepository.checkLockedAccount(tentaikhoan);
        return result;
    }
}
