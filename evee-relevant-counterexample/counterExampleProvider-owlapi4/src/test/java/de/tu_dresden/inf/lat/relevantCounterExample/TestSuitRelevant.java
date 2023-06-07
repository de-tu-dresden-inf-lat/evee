package de.tu_dresden.inf.lat.relevantCounterExample;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)

@Suite.SuiteClasses({ loopTest.class, NotLoopTest.class, RedundancyTest.class, TestCycle.class, TestCycle2.class,
		TestRelevantA.class, TestRelevantC.class, TestRelevantD.class, TestSquare.class,
		RelevantCounterExamplesTest.class })

public class TestSuitRelevant {

	public TestSuitRelevant() {
		// TODO Auto-generated constructor stub
	}

}
