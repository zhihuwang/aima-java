/*
 * Created on Aug 2, 2005
 *
 */
package aima.test.learningtest;

import java.util.List;

import aima.learning.framework.DataSet;
import aima.learning.inductive.DLTest;
import aima.learning.inductive.DLTestFactory;

public class MockDLTestFactory extends DLTestFactory {

	private  List<DLTest> tests;

	public MockDLTestFactory(List<DLTest> tests) {
		this.tests = tests;
	}
	public  List<DLTest> createDLTestsWithAttributeCount(DataSet ds,int i){
		return tests;
	}

}