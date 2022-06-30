package org.iii.esd.server.domain.trial;

import lombok.Getter;

import org.iii.esd.exception.EnumInitException;

public enum GatewayCommand {
    NORMAL("normal"),
    UNLOAD("unload");

    @Getter
    private String command;

    GatewayCommand(String command) {
        this.command = command;
    }

    public static GatewayCommand ofCommand(String command) {
        for (GatewayCommand value : values()) {
            if (value.getCommand().equals(command)) {
                return value;
            }
        }

        throw new EnumInitException(GatewayCommand.class, command);
    }
}
