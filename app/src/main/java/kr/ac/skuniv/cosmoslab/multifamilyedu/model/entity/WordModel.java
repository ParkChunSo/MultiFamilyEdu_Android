package kr.ac.skuniv.cosmoslab.multifamilyedu.model.entity;

import lombok.Builder;
import lombok.Data;

/**
 * Created by chunso on 2019-01-02.
 */

@Data @Builder
public class WordModel {
    String word;

    String pass;

    int highestScore;
}
