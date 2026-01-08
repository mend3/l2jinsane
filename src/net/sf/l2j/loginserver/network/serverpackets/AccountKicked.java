/**/
package net.sf.l2j.loginserver.network.serverpackets;

public final class AccountKicked extends L2LoginServerPacket {
    private final AccountKicked.AccountKickedReason _reason;

    public AccountKicked(AccountKicked.AccountKickedReason reason) {
        this._reason = reason;
    }

    protected void write() {
        this.writeC(2);
        this.writeD(this._reason.getCode());
    }

    public enum AccountKickedReason {
        REASON_DATA_STEALER(1),
        REASON_GENERIC_VIOLATION(8),
        REASON_7_DAYS_SUSPENDED(16),
        REASON_PERMANENTLY_BANNED(32);

        private final int _code;

        AccountKickedReason(int param3) {
            this._code = param3;
        }

        public final int getCode() {
            return this._code;
        }
    }
}