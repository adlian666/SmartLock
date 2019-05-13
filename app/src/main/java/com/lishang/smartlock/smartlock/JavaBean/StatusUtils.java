package com.lishang.smartlock.smartlock.JavaBean;

import com.lishang.smartlock.R;

public class StatusUtils {
    public static  String setStatus(Integer useStatus,Integer enableStatus,Integer onlineStatus){
        switch (useStatus) {
            case 0:
                //未激活
                String str = "未激活状态";
                return str;
            case 1:
                if (enableStatus == 10) {
                    //在线状态
                    switch (onlineStatus) {
                        case 100: {
                           return "关闭状态";
                        }
                        case 101: {
                            return"打开状态";
                        }
                        case 102: {
                            return"异常打开";
                        }
                        case -1: {
                            return"未知";
                        }
                    }
                } else {
                    //离线状态
                    return"离线状态";
                }
                break;
            case 2: {
                return "停用状态";
            }
            case 3: {
                return "废弃状态";
            }
        }
        return "";
    }
}
