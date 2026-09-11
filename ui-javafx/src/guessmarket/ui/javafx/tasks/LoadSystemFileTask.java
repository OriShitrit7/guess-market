package guessmarket.ui.javafx.tasks;

import guessmarket.engine.api.MarketManager;
import guessmarket.engine.exception.file.InvalidFileException;
import javafx.concurrent.Task;

public class LoadSystemFileTask extends Task<Boolean> {

    private static final int PROGRESS_STEP_COUNT = 3;
    private static final long PROGRESS_STEP_DELAY_MS = 500;

    private final MarketManager marketManager;
    private final String pathName;
    private String errorMessage;

    public LoadSystemFileTask(MarketManager marketManager, String pathName) {
        this.marketManager = marketManager;
        this.pathName = pathName;
    }

    @Override
    protected Boolean call() throws Exception {
        updateProgress(0, PROGRESS_STEP_COUNT);
        Thread.sleep(PROGRESS_STEP_DELAY_MS);
        updateProgress(1, PROGRESS_STEP_COUNT);

        try {
            marketManager.loadSystemFile(pathName);
        }
        catch (InvalidFileException e) {
            errorMessage = e.getMessage();
            return false;
        }

        updateProgress(2, PROGRESS_STEP_COUNT);
        Thread.sleep(PROGRESS_STEP_DELAY_MS);
        updateProgress(PROGRESS_STEP_COUNT, PROGRESS_STEP_COUNT);
        return true;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
