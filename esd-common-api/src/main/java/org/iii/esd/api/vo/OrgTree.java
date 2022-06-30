package org.iii.esd.api.vo;

import java.util.List;
import java.util.Objects;

import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections.CollectionUtils;

import org.iii.esd.exception.EnumInitException;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrgTree {

    private Unit myUnit;

    public enum Type {
        SYS,
        QSE,
        TXG,
        RES;

        public static Type ofName(String name) {
            for (Type value : values()) {
                if (Objects.equals(value.name(), name)) {
                    return value;
                }
            }

            throw new EnumInitException(Type.class, name);
        }
    }

    @Getter
    @Setter
    @ToString
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Unit {
        private Type unitType;
        private String unitName;
        private String unitId;
        private Long sysId;
        private Integer unitCode;
        private List<Unit> subUnits;
        private Integer serviceType;
        private Integer resourceType;

        public List<Unit> toList() {
            var unitListBuilder = ImmutableList.<Unit>builder();
            unitListBuilder.add(this);

            if (CollectionUtils.isNotEmpty(this.subUnits)) {
                unitListBuilder.addAll(this.subUnits);

                for (Unit subUnit : this.subUnits) {
                    if (CollectionUtils.isNotEmpty(subUnit.subUnits)) {
                        unitListBuilder.addAll(subUnit.subUnits);
                    }
                }
            }

            return unitListBuilder.build();
        }
    }
}
