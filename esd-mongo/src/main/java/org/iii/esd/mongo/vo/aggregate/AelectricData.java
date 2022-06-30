package org.iii.esd.mongo.vo.aggregate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.iii.esd.mongo.document.FieldProfile;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AelectricData {

    private FieldProfile fieldId;

    private Boolean needFix;

    private int count;

}