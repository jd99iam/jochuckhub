package com.guenbon.jochuckhub.dto.response;

import com.guenbon.jochuckhub.entity.TeamRole;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamResponseDTO {
    private String teamName;
    private List<TeamMemberDTO> members;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeamMemberDTO {
        private Long memberId;
        private String username;
        private TeamRole role;
    }
}
