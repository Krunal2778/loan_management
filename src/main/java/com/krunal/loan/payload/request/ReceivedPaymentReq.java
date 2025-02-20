package com.krunal.loan.payload.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReceivedPaymentReq {

    @NotNull(message = "EMI ID is required")
    private Long emiId;

    @NotNull(message = "Amount received is required")
    @Positive(message = "Amount received must be positive")
    private Double amountReceivedAmount;

    @NotNull(message = "Payment type is required")
    private Long paymentType;

    @Size(max = 300, message = "Notes must be less than 300 characters")
    private String notes;

    @NotNull(message = "Status is required")
    private Long statusId;

    private String base64Image;

    private Long paymentReceivedUser;

    private String emiDate;

    @NotNull(message = "Payment received date is required")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private String paymentReceivedDate;
}