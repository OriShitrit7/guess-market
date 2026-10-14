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

// Holds the Account tab: pulls the logged-in user's account details from the server and hands them
// to the wallet and to the events panel.
public class AccountController implements Closeable {
    private static final String ACCOUNT_BLOCKED = "Account Blocked";
    private static final String BLOCKED_MESSAGE = "Your balance is now negative. Your account has been blocked and cannot perform further actions.";

    @FXML private WalletController walletComponentController;
    @FXML private UsersTableController usersTableComponentController;
    @FXML private MyEventsController myEventsComponentController;

    private Timer timer;
    private TimerTask accountRefresher;
    private boolean blocked;

    // Starts pulling the user's account details and the users list from the server.
    public void setActive() {
        accountRefresher = new AccountRefresher(details -> Platform.runLater(() -> showAccount(details)));
        timer = new Timer();
        timer.schedule(accountRefresher, Constants.REFRESH_RATE, Constants.REFRESH_RATE);
        timer.schedule(createDisplayedEventRefresher(), Constants.REFRESH_RATE, Constants.REFRESH_RATE);

        usersTableComponentController.startUsersTableRefresher();
    }

    // The displayed event can change without any change in the user's account, for example when another
    // user trades in it, so it is pulled on its own timer task.
    private TimerTask createDisplayedEventRefresher() {
        return new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(myEventsComponentController::refreshDisplayedEvent);
            }
        };
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
