package com.krunal.loan.payload.response;

import com.krunal.loan.models.LoanContributor;
import com.krunal.loan.models.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PartnerList {
    private List<User> userList;

}
