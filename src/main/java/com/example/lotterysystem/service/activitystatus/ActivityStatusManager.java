package com.example.lotterysystem.service.activitystatus;

import com.example.lotterysystem.service.dto.ConvertActivityStatusDTO;

public interface ActivityStatusManager {
    void handlerEvent(ConvertActivityStatusDTO convertActivityStatusDTO);

    void rollbackHandlerEvent(ConvertActivityStatusDTO convertActivityStatusDTO);
}
