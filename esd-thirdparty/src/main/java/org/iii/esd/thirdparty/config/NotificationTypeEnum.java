package org.iii.esd.thirdparty.config;

import lombok.Getter;

@Getter
public enum NotificationTypeEnum {

	AO_NOTICE("N", "DNP_AO_notice", 0, "SpinReserveNotice.ftl"),
    DO_ALERT_1("A", "DNP_DO_alert", 1, "SpinReserveAlert.ftl"),
    DO_EVENT_1("E", "DNP_DO_event", 2, null),
    AI_SENSING("S", "DNP_AI_sensing", 3, null),
    DI_CONFIRM_1("C", "DNP_DI_confirm", 4, null),
    DI_READY_1("R", "DNP_DI_ready", 5, null),
    DI_QUIT_1("Q", "DNP_DI_quit", 6, null),
    SYS_VM_1("V1", "SYS_vm_1", 7, null),
    SYS_GW_1("G1", "SYS_gw_1", 8, null),
    AO_END("D", "DNP_AO_end", 9, "SpinReserveEnd.ftl"),
    AO_STOP("P", "DNP_AO_stop", 10, "SpinReserveStop.ftl"),
    DI_START_SERVICE("DI0", "DNP_DI_START_SERVICE", 11, "StartService.ftl"),
    DI_STOP_SERVICE("DI1", "DNP_DI_STOP_SERVICE", 12, "StopService.ftl"),
    DI_START_STAND_BY("DI4", "DNP_DI_START_STAND_BY", 13, "StartStandBy.ftl"),
    DI_STOP_STAND_BY("DI5", "DNP_DI_STOP_STAND_BY", 14, "StopStandBy.ftl"),
	ABANDON_STAND_BY("ASB", "ABANDON_STAND_BY", 15, "AbansonStandBy.ftl"),

    UNDEFINED("U", "undefined", -1, null),
	;
	
	private NotificationTypeEnum(String code, String name, int index, String ftl) {
		this.code = code;
		this.name = name;
		this.index = index;
		this.ftl = ftl;
	}
	
	public static NotificationTypeEnum of(String code) {
		for (NotificationTypeEnum entity: values()) {
			if (entity.getCode().equals(code))
				return entity;
		}
		return UNDEFINED;
	}
	
	private String code;

	private String name;

	private int index;
	
	private String ftl;

}