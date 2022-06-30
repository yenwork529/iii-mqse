package org.iii.esd.mongo.document;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Document(collection = "TransactionGroupProfile")

public class TransactionGroupProfile extends SequenceDocument {
    private BigInteger txGuid;
    private Integer qseId;
    private String asType;

    public TransactionGroupProfile(BigInteger txGuid){
        this.txGuid = txGuid;
    }

    public Boolean containsResId(Integer resId){
		return true;
	}

    public Boolean isDregType(){
        return true;
    }

    public Boolean isReadyToFire(){
        return true;
    }
}