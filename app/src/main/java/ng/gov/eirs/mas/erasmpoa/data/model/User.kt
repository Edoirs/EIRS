package ng.gov.eirs.mas.erasmpoa.data.model

import android.text.TextUtils
import ng.gov.eirs.mas.erasmpoa.data.constant.UserCode
import ng.gov.eirs.mas.erasmpoa.util.getInitials
import java.io.Serializable

class User : Serializable {
    val employeeId: Long = 0
    val organizationId: Long = 0
    val parentOrganizationId: Long = 0
    val organizationName: String = ""
    val firstName: String = ""
    val lastName: String = ""
    val email: String = ""
    val businessPhone: String = ""
    val userName: String = ""
    val empRoleList: String = ""
    val city: Long = 0
    val area: Long = 0
    val state: Long = 0
    val country: Long = 0
    val stateName: String = ""
    val cityName: String = ""
    val areaName: String = ""
    val imageName: String = ""
    val imagePath: String = ""
    val addressLine1: String = ""
    val roleId = 0
    val code: String = ""
    val Description: String = ""

    var passwordStatus = 0

    fun isAppUser(): Boolean = code == UserCode.APP_USER

    private fun hasAccessTo(requestedRole: String): Boolean {
        val explodedRoles = empRoleList.split(",").toTypedArray()
        for (roleId in explodedRoles) {
            if (roleId == requestedRole) {
                return true
            }
        }
        return false
    }

    val fullName: String
        get() = "$firstName $lastName"

    val organizationInitials: String
        get() = organizationName.getInitials()

    val nameInitials: String
        get() = getInitials(firstName, lastName)

    val address: String
        get() {
            val addressBuilder = StringBuilder()
            if (!TextUtils.isEmpty(addressLine1)) {
                addressBuilder.append(addressLine1)
            }
            if (!TextUtils.isEmpty(cityName)) {
                if (!TextUtils.isEmpty(addressBuilder.toString())) {
                    addressBuilder.append(", ")
                }
                addressBuilder.append(cityName)
            }
            if (!TextUtils.isEmpty(areaName)) {
                if (!TextUtils.isEmpty(addressBuilder.toString())) {
                    addressBuilder.append(", ")
                }
                addressBuilder.append(areaName)
            }
            if (!TextUtils.isEmpty(stateName)) {
                if (!TextUtils.isEmpty(addressBuilder.toString())) {
                    addressBuilder.append(", ")
                }
                addressBuilder.append(stateName)
            }
            return addressBuilder.toString()
        }
}