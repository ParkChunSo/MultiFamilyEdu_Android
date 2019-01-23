package kr.ac.skuniv.cosmoslab.multifamilyedu.model.entity;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * MultiFamilyEdu_Android
 * Class: UserModel
 * Created by youngjun on 2018-11-27.
 * <p>
 * Description:
 */
@Getter
@Setter @Builder
public class UserModel implements Serializable {
    @SerializedName("userid")
    private String id;
    @SerializedName("pw")
    private String pw;
    @SerializedName("name")
    private String name;
    @SerializedName("mobile")
    private String mobile;
    @SerializedName("level")
    private String level;
    @SerializedName("count")
    private String count;
    @SerializedName("response")
    private String response;
}
