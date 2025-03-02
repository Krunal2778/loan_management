package com.krunal.loan.service.impl;

import com.krunal.loan.common.DateUtils;
import com.krunal.loan.models.Borrower;
import com.krunal.loan.models.Loan;
import com.krunal.loan.models.LoanStatus;
import com.krunal.loan.payload.response.DashBoardLoanCountsResponse;
import com.krunal.loan.repository.BorrowerRepository;
import com.krunal.loan.repository.LoanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

@Service
public class DashBoardService {
    private static final Logger logger = LoggerFactory.getLogger(DashBoardService.class);
    private final BorrowerRepository borrowerRepository;
    private final LoanRepository loanRepository;

    public DashBoardService(BorrowerRepository borrowerRepository, LoanRepository loanRepository) {
        this.borrowerRepository = borrowerRepository;
        this.loanRepository = loanRepository;
    }

    public DashBoardLoanCountsResponse getDashBoardLoanCountsResponse(String startDate, String endDate) {
        logger.info("Entering getDashBoardLoanCountsResponse with startDate: {} and endDate: {}", startDate, endDate);

        DashBoardLoanCountsResponse response = new DashBoardLoanCountsResponse();
        LocalDate localDateStart = DateUtils.getDateFromString(startDate, DateUtils.YMD);
        LocalDate localDateEnd = DateUtils.getDateFromString(endDate, DateUtils.YMD);

        logger.debug("Converted startDate: {} to LocalDate: {}", startDate, localDateStart);
        logger.debug("Converted endDate: {} to LocalDate: {}", endDate, localDateEnd);

        Integer totalBorrowers = borrowerRepository.findAll().size();
        logger.debug("Total borrowers fetched: {}", totalBorrowers);

        AtomicReference<Integer> totalLoanAccounts = new AtomicReference<>(0);
        AtomicReference<Integer> activeLoanAccounts = new AtomicReference<>(0);
        AtomicReference<Integer> closedLoanAccounts = new AtomicReference<>(0);
        AtomicReference<Double> totalLoanAmount = new AtomicReference<>(0.0);
        AtomicReference<Double> activeLoanAmount = new AtomicReference<>(0.0);
        AtomicReference<Double> closedLoanAmount = new AtomicReference<>(0.0);

        Predicate<Loan> isActiveLoan = loan -> loan.getStatus().equals(LoanStatus.ACTIVE.getCode());
        Predicate<Loan> isClosedLoan = loan -> loan.getStatus().equals(LoanStatus.CLOSED.getCode());
        Predicate<Loan> isNotRejectedLoan = loan -> !loan.getStatus().equals(LoanStatus.REJECTED.getCode());

        List<Loan> loans = getLoansBetweenDates(localDateStart, localDateEnd);
        logger.debug("Fetched {} loans between {} and {}", loans.size(), localDateStart, localDateEnd);

        loans.forEach(loan -> {
            if (isActiveLoan.test(loan)) {
                activeLoanAccounts.set(activeLoanAccounts.get() + 1);
                activeLoanAmount.set(activeLoanAmount.get() + loan.getLoanAmount());
            }
            if (isClosedLoan.test(loan)) {
                closedLoanAccounts.set(closedLoanAccounts.get() + 1);
                closedLoanAmount.set(closedLoanAmount.get() + loan.getLoanAmount());
            }
            if (isNotRejectedLoan.test(loan)) {
                totalLoanAccounts.set(totalLoanAccounts.get() + 1);
                totalLoanAmount.set(totalLoanAmount.get() + loan.getLoanAmount());
            }
        });

        logger.debug("Active loan accounts: {}", activeLoanAccounts.get());
        logger.debug("Closed loan accounts: {}", closedLoanAccounts.get());
        logger.debug("Total loan accounts: {}", totalLoanAccounts.get());
        logger.debug("Active loan amount: {}", activeLoanAmount.get());
        logger.debug("Closed loan amount: {}", closedLoanAmount.get());
        logger.debug("Total loan amount: {}", totalLoanAmount.get());

        response.setActiveLoanAccountsIncrease(calculateLoanAccountIncreasePercentage(isActiveLoan));
        response.setClosedLoanAccountsIncrease(calculateLoanAccountIncreasePercentage(isClosedLoan));
        response.setTotalLoanAccountsIncrease(calculateLoanAccountIncreasePercentage(isNotRejectedLoan));
        response.setTotalBorrowersIncrease(calculateBorrowerIncreasePercentage());
        response.setTotalBorrowers(totalBorrowers);
        response.setTotalLoanAccounts(totalLoanAccounts.get());
        response.setActiveLoanAccounts(activeLoanAccounts.get());
        response.setClosedLoanAccounts(closedLoanAccounts.get());
        response.setTotalLoanAmount(totalLoanAmount.get());
        response.setActiveLoanAmount(activeLoanAmount.get());
        response.setClosedLoanAmount(closedLoanAmount.get());

        logger.info("Exiting getDashBoardLoanCountsResponse with response: {}", response);
        return response;
    }

    public float calculateLoanAccountIncreasePercentage(Predicate<Loan> statusPredicate) {
        logger.info("Entering calculateLoanAccountIncreasePercentage with statusPredicate");

        LocalDate today = LocalDate.now();
        logger.debug("Today's date: {}", today);

        // Calculate the start and end dates of the current month period (1st to today)
        LocalDate startOfCurrentMonth = today.withDayOfMonth(1);
        LocalDate endOfCurrentMonth = today; // Use today's date as the end

        // Calculate the start and end dates of the previous month period (1st to the same day as today)
        LocalDate startOfPreviousMonth = startOfCurrentMonth.minusMonths(1);
        LocalDate endOfPreviousMonth = startOfPreviousMonth.withDayOfMonth(today.getDayOfMonth());

        logger.debug("Current month period: {} to {}", startOfCurrentMonth, endOfCurrentMonth);
        logger.debug("Previous month period: {} to {}", startOfPreviousMonth, endOfPreviousMonth);

        // Fetch loans for the current and previous month periods using Specifications
        Specification<Loan> currentMonthSpec = LoanSpecifications.loanDateBetween(startOfCurrentMonth, endOfCurrentMonth);
        Specification<Loan> previousMonthSpec = LoanSpecifications.loanDateBetween(startOfPreviousMonth, endOfPreviousMonth);

        List<Loan> currentMonthLoans = loanRepository.findAll(currentMonthSpec);
        List<Loan> previousMonthLoans = loanRepository.findAll(previousMonthSpec);

        logger.debug("Fetched {} loans for the current month", currentMonthLoans.size());
        logger.debug("Fetched {} loans for the previous month", previousMonthLoans.size());

        // Filter loans using the provided Predicate
        currentMonthLoans = currentMonthLoans.stream()
                .filter(statusPredicate) // Apply the Predicate
                .toList();

        previousMonthLoans = previousMonthLoans.stream()
                .filter(statusPredicate) // Apply the Predicate
                .toList();

        logger.debug("Filtered current month loans: {}", currentMonthLoans.size());
        logger.debug("Filtered previous month loans: {}", previousMonthLoans.size());

        // Count the number of loans
        int currentMonthLoanCount = currentMonthLoans.size();
        int previousMonthLoanCount = previousMonthLoans.size();

        // Handle edge case where there were no loans in the previous month
        if (previousMonthLoanCount == 0) {
            logger.debug("No loans in the previous month. Returning 100.0 if current month has loans, otherwise 0.0");
            return (float) (currentMonthLoanCount > 0 ? 100.0 : 0.0);
        }

        // Calculate and return the percentage increase
        float increasePercentage = (float) (((double) (currentMonthLoanCount - previousMonthLoanCount) / previousMonthLoanCount) * 100);
        logger.debug("Calculated percentage increase: {}", increasePercentage);

        logger.info("Exiting calculateLoanAccountIncreasePercentage with result: {}", increasePercentage);
        return increasePercentage;
    }

    public List<Loan> getLoansBetweenDates(LocalDate startDate, LocalDate endDate) {
        logger.info("Entering getLoansBetweenDates with startDate: {} and endDate: {}", startDate, endDate);

        // Use the Specification to filter loans
        Specification<Loan> spec = LoanSpecifications.loanDateBetween(startDate, endDate);
        List<Loan> loans = loanRepository.findAll(spec);

        logger.debug("Fetched {} loans between {} and {}", loans.size(), startDate, endDate);
        logger.info("Exiting getLoansBetweenDates");
        return loans;
    }

    public float calculateBorrowerIncreasePercentage() {
        logger.info("Entering calculateBorrowerIncreasePercentage");

        LocalDate today = LocalDate.now();
        logger.debug("Today's date: {}", today);

        // Calculate the start and end dates of the current month period (1st to today)
        LocalDate startOfCurrentMonth = today.withDayOfMonth(1);
        LocalDate endOfCurrentMonth = today; // Use today's date as the end

        // Calculate the start and end dates of the previous month period (1st to the same day as today)
        LocalDate startOfPreviousMonth = startOfCurrentMonth.minusMonths(1);
        LocalDate endOfPreviousMonth = startOfPreviousMonth.withDayOfMonth(today.getDayOfMonth());

        logger.debug("Current month period: {} to {}", startOfCurrentMonth, endOfCurrentMonth);
        logger.debug("Previous month period: {} to {}", startOfPreviousMonth, endOfPreviousMonth);

        // Fetch borrower for the current and previous month periods using Specifications
        Specification<Borrower> currentMonthSpec = BorrowerSpecifications.loanDateBetween(startOfCurrentMonth, endOfCurrentMonth);
        Specification<Borrower> previousMonthSpec = BorrowerSpecifications.loanDateBetween(startOfPreviousMonth, endOfPreviousMonth);

        List<Borrower> currentMonthLoans = borrowerRepository.findAll(currentMonthSpec);
        List<Borrower> previousMonthLoans = borrowerRepository.findAll(previousMonthSpec);


        // Count the number of borrower
        int currentMonthLoanCount = currentMonthLoans.size();
        int previousMonthLoanCount = previousMonthLoans.size();

        logger.debug("Fetched {} borrower for the current month", currentMonthLoanCount);
        logger.debug("Fetched {} borrower for the previous month", previousMonthLoanCount);
        // Handle edge case where there were no loans in the previous month
        if (previousMonthLoanCount == 0) {
            logger.debug("No loans in the previous month. Returning 100.0 if current month has loans, otherwise 0.0");
            return (float) (currentMonthLoanCount > 0 ? 100.0 : 0.0);
        }

        // Calculate and return the percentage increase
        float increasePercentage = (float) (((double) (currentMonthLoanCount - previousMonthLoanCount) / previousMonthLoanCount) * 100);
        logger.debug("Calculated percentage increase: {}", increasePercentage);

        logger.info("Exiting calculateBorrowerIncreasePercentage with result: {}", increasePercentage);
        return increasePercentage;
    }
}