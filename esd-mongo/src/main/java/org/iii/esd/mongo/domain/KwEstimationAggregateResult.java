package org.iii.esd.mongo.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import org.iii.esd.mongo.document.UuidDocument;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class KwEstimationAggregateResult extends UuidDocument {

    long seconds;
    long count;
    double value;
}
