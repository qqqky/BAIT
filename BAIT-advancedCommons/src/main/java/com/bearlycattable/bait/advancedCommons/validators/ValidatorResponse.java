package com.bearlycattable.bait.advancedCommons.validators;

import com.bearlycattable.bait.commons.enums.SeedMutationTypeEnum;

import javafx.scene.control.Control;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ValidatorResponse {

    String errorMessage;
    Control errorTargetTextField;
    OptionalConfigValidationResponseType responseType;
    Object responseData; //intentional
    SeedMutationTypeEnum seedMutationType;

}
