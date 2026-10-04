import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

public class Log4jSmokeTest {
    private static final Logger logger = LogManager.getLogger(Log4jSmokeTest.class);

    public static void main(String[] args) {
        Package apiPkg = org.apache.logging.log4j.Logger.class.getPackage();
        Package corePkg = LoggerContext.class.getPackage();
        System.out.println("log4j-api Implementation-Version=" + apiPkg.getImplementationVersion());
        System.out.println("log4j-core Implementation-Title=" + corePkg.getImplementationTitle());
        System.out.println("log4j-core Implementation-Version=" + corePkg.getImplementationVersion());

        logger.info("Log4j smoke test OK");
        logger.debug("Debug message from smoke test");

        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        System.out.println("LoggerContext name=" + ctx.getName());
        System.out.println("Config location=" + ctx.getConfiguration().getConfigurationSource());
        System.out.println("SMOKE_TEST_PASSED");
    }
}
