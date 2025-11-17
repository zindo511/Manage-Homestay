package vn.huy.controller.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomImageResponse {
    private Long id;
    private Long roomId;
    private String imageUrl;
}
