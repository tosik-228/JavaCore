package API_Tested.JMeter;

import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.SetupThreadGroup;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.util.SSLManager;
import org.apache.jorphan.collections.HashTree;

public class My_REST_app {

    public static void main(String[] args) {
        // Initialize JMeter
        JMeterUtils.loadJMeterProperties("src/main/java/API_Tested/JMeter/jmeter.json");
        JMeterUtils.initLocale();

        // Initialize SSL if needed
        SSLManager sslManager = SSLManager.getInstance();
        sslManager.setContext(null);

        // Create a TestPlan
        HashTree testPlanTree = new HashTree();
        TestPlan testPlan = new TestPlan("Test Plan");
        testPlanTree.add(testPlan);

        // Create an HTTP Sampler for registration
        HTTPSampler httpSampler = new HTTPSampler();
        httpSampler.setDomain("http://localhost"); // Replace with your API domain
        httpSampler.setPort(8070); // Replace with your API port
        httpSampler.setPath("/api/v1/auth/registration");
        httpSampler.setMethod("POST");

        // Add the HTTP Sampler to the TestPlan
        testPlanTree.add(testPlan, httpSampler);

        // Create a ThreadGroup
        SetupThreadGroup threadGroup = new SetupThreadGroup();
        threadGroup.setNumThreads(1); // Number of users
        threadGroup.setRampUp(1); // Ramp-up period in seconds
        testPlanTree.add(testPlan, threadGroup);

        // Run the TestPlan
        StandardJMeterEngine jmeter = new StandardJMeterEngine();
        jmeter.configure(testPlanTree);
        jmeter.run();
    }
}