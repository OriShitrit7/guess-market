package guessmarket.client.components.account;

import guessmarket.client.components.account.accountdetails.myevents.MyEventsController;
import guessmarket.client.components.account.accountdetails.wallet.WalletController;
import guessmarket.client.components.account.userstable.UsersTableController;
import guessmarket.client.components.dialogs.Dialogs;
import guessmarket.client.util.Constants;
import guessmarket.dto.AccountDetailsDto;
import guessmarket.dto.UserSummaryDto;
import javafx.application.Platform;
import javafx.fxml.FXML;

import java.io.Closeable;
import java.math.BigDecimal;
import java.util.Timer;
import java.util.TimerTask;

// Manages user selection, account history and event trading in the Account tab.
public class AccountController implements Closeable {
    private static final String ACCOUNT_BLOCKED = "Account Blocked";
    private static final String BLOCKED_MESSAGE = "Your balance is now negative. Your account has been blocked and cannot perform further actions.";

    @FXML private WalletController walletComponentController;
    @FXML private UsersTableController usersTableComponentController;
    @FXML private MyEventsController myEventsComponentController;

    private Timer timer;
    private TimerTask accountRefresher;
    private boolean blocked;

    public void setActive() {
        accountRefresher = new AccountRefresher(details -> Platform.runLater(() -> showAccount(details)));
        timer = new Timer();
        timer.schedule(accountRefresher, Constants.REFRESH_RATE, Constants.REFRESH_RATE);

        usersTableComponentController.startUsersTableRefresher();
    }

    private void showAccount(AccountDetailsDto details) {
        walletComponentController.setUserName(details.getSummary().getName());
        walletComponentController.setBalance(BigDecimal.valueOf(details.getSummary().getBalance()));
        walletComponentController.addTransactions(details.getTransactions());
        myEventsComponentController.update(details.getSummary(), details.getEvents());
        warnIfNewlyBlocked(details.getSummary());
    }

    // The engine allows a balance to go negative and blocks the user; the user has to be told.
    private void warnIfNewlyBlocked(UserSummaryDto summary) {
        boolean wasBlocked = blocked;

        blocked = summary.isBlocked();
        if (!wasBlocked && blocked) {
            Dialogs.showWarning(ACCOUNT_BLOCKED, BLOCKED_MESSAGE);
        }
    }

    @Override
    public void close() {
        usersTableComponentController.close();

        if (accountRefresher != null && timer != null) {
            accountRefresher.cancel();
            timer.cancel();
        }
    }
}
