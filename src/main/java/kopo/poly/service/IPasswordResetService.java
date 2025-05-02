package kopo.poly.service;

import kopo.poly.dto.PasswordChangeDTO;
import kopo.poly.dto.PasswordResetDTO;

public interface IPasswordResetService {

    boolean sendTempPassword(PasswordResetDTO pDTO) throws Exception;

    boolean changePassword(PasswordChangeDTO pDTO) throws Exception;
}
