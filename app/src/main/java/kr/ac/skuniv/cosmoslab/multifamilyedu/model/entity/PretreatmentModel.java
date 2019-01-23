package kr.ac.skuniv.cosmoslab.multifamilyedu.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by chunso on 2018-12-26.
 */

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class PretreatmentModel {
    private int[] waveData;

    private int startIndex;

    private int endIndex;

}
