package kr.ac.skuniv.cosmoslab.multifamilyedu.model.entity;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by chunso on 2018-11-19.
 */

@Data @Builder
@NoArgsConstructor @AllArgsConstructor
public class WaveFormModel {
    private int[] waveData;
    private int[] firstSlopeData;
    private int[] secondSlopeData;
    private int maximumValueIndex;

    private List<Integer> checkPoints;
    private List<Integer> hidenCheckPoints;
    private List<Integer> finalExtremePoints;
}
