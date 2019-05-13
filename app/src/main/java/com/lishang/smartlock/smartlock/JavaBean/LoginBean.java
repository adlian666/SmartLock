package com.lishang.smartlock.smartlock.JavaBean;

public class LoginBean {
    public String getSmartLockId() {
        return SmartLockId;
    }

    public void setSmartLockId( String smartLockId ) {
        SmartLockId = smartLockId;
    }

    public String getLoginAccount() {
        return loginAccount;
    }

    public void setLoginAccount( String loginAccount ) {
        this.loginAccount = loginAccount;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName( String userName ) {
        this.userName = userName;
    }

    public String getCreateAt() {
        return createAt;
    }

    public void setCreateAt( String createAt ) {
        this.createAt = createAt;
    }

    public String getState() {
        return state;
    }

    public void setState( String state ) {
        this.state = state;
    }

    public String getMaxAgeInSeconds() {
        return maxAgeInSeconds;
    }

    public void setMaxAgeInSeconds( String maxAgeInSeconds ) {
        this.maxAgeInSeconds = maxAgeInSeconds;
    }

    public String SmartLockId;
    public String loginAccount;
    public String userName;
    public String createAt;
    public String state;
    public String maxAgeInSeconds;

}
