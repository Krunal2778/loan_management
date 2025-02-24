package com.krunal.loan.payload.response;

import com.krunal.loan.models.Emi;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class EmiListByDateResponse {
    List<Emi> upcomingEmis;
    List<Emi> receivedEmis;
    List<Emi> bouncedEmis;
}
