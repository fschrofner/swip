package at.fhhgb.mc.swip.trigger;

/**
 * Container class which saves the amount of milliseconds all trigger should be ignored
 * and the time when the timeout was started.
 *
 * @author Florian Schrofner & Dominik Koeltringer
 *
 */
public class TriggerTimeout {
    private long startTime;
    private long timeFrame;

    /**
     * Initialises a new trigger timeout.
     * @param _timeFrame the number of milliseconds in which no trigger should be applied.
     *                   -1 = infinity
     */
    public TriggerTimeout(long _timeFrame){
        startTime = System.currentTimeMillis();
        timeFrame = _timeFrame;
    }

    /**
     * Tells you if the triggers should still be ignored.
     * @return true = triggers should still be ignored, false = start to check triggers again.
     */
    public boolean timedOut(){
        long currentTime = System.currentTimeMillis();
        return timeFrame < 0 || (currentTime - startTime < timeFrame);
    }

    /**
     * Returns the number of milliseconds the trigger is still active.
     * If the number is negative, the timeout is already invalid.
     * @return the number of remaining milliseconds.
     */
    public long getTimeRemaining(){
        long currentTime = System.currentTimeMillis();
        if(timeFrame < 0){
            return -1;
        } else {
            return timeFrame - (currentTime - startTime);
        }

    }
}
