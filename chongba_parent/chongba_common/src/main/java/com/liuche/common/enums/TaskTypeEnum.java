package com.liuche.common.enums;

import com.liuche.common.entity.StatusCode;
import lombok.Getter;

/**
 *状态码 类型及业务对应关系
 */
@Getter
public enum TaskTypeEnum {

	ORDER_REQ_FAILED(StatusCode.ORDER_REQ_FAILED, 1001, 1,"话费充值失败，重试"),
	BALANCE_NOT_ENOUGH(StatusCode.BALANCE_NOT_ENOUGH, 1001, 2,"供应商余额不足，轮转其他供应商"),
	REMOTEERROR(StatusCode.REMOTEERROR, 1001, 3,"远程调用失败，重试"),
	STATECHECK(StatusCode.STATECHECK, 1001, 4,"检查订单状态");

	private final int errorCode; //错误码

	private final int taskType; //对应具体业务

	private final int priority;  //业务不同级别

	private final String desc;   //描述信息

	TaskTypeEnum(int errorCode, int taskType, int priority, String desc) {
		this.errorCode=errorCode;
		this.taskType = taskType;
		this.priority = priority;
		this.desc = desc;
	}

	/**
	 * 获取当前错误任务类型
	 * @param errorCode
	 * @return
	 */
	public static TaskTypeEnum getTaskType(int errorCode) {
		for (TaskTypeEnum c : TaskTypeEnum.values()) {
			if (c.getErrorCode() == errorCode) {
				return c;
			}
		}
		return null;
	}
}
