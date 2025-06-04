package com.olh.feeds.service.n8n;

public enum CodeNodeSelectionStrategy {
    FIRST_AVAILABLE,        // Chọn Code node đầu tiên tìm thấy
    LAST_CREATED,          // Chọn Code node được tạo cuối cùng (theo position)
    NO_CONNECTIONS,        // Chọn Code node chưa có connection nào
    BY_NAME,               // Chọn theo tên cụ thể
    BY_ID,                 // Chọn theo ID cụ thể
    CLOSEST_POSITION       // Chọn Code node gần nhất về vị trí
}
