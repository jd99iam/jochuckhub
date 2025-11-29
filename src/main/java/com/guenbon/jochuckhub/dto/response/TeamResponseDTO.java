package com.guenbon.jochuckhub.dto.response;

import com.guenbon.jochuckhub.entity.TeamRole;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class TeamResponseDTO {
    private String teamName;
    private List<TeamMemberDTO> members;

    @Getter
    @Setter
    @Builder
    public static class TeamMemberDTO {
        private Long memberId;
        private String username;
        private TeamRole role;
    }
}
