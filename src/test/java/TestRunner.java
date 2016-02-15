import controller.MultiplePublishersAndMultipleSubscriberTest;
import controller.SinglePublisherAndSingleSubscriberTest;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.VWAPServiceTest;

public class TestRunner {

    private final static Logger logger = (Logger) LoggerFactory.getLogger(TestRunner.class);

    public static void main(String[] args) {
        Result result = JUnitCore.runClasses(VWAPServiceTest.class,
                SinglePublisherAndSingleSubscriberTest.class,
                MultiplePublishersAndMultipleSubscriberTest.class);
        for (Failure failure : result.getFailures()) {
            logger.debug(failure.toString());
        }
        logger.debug("All tests passed: " + result.wasSuccessful());
    }
}