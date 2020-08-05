package cn.houlang.rvds.permission;

/**
 * @author #Suyghur.
 * @date 2020/7/8
 */
public interface PermissionGrantedListener {
    /**
     * 申请结果回调
     *
     * @param grantedPermissions 同意的权限组
     * @param deniedPermissions  拒绝的权限组
     */
    void onPermissionsResult(String[] grantedPermissions, String[] deniedPermissions);
}
