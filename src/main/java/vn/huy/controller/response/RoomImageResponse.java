package vn.huy.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomImageResponse {
    private Long id;
    private Long roomId;
    private String imageUrl;
}
