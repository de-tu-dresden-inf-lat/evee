package de.tu_dresden.inf.lat.relevantCounterExample;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)

@Suite.SuiteClasses({ RefinerMapperTest.class, NotLoopTest.class, RedundancyTest.class, TestCycle.class,
		TestCycle2.class, TestCycle3.class, PluginRelatedTests.class, ELKCounterModelTest.class,
		TestRelevantA.class, TestRelevantB.class, TestRelevantC.class, TestRelevantD.class, TestSquare.class,
		RelevantCounterExampleGeneratorTest.class, TrackerTest.class})

public class TestSuitRelevant {

	public TestSuitRelevant() {
		// TODO Auto-generated constructor stub
	}

}
