/*
 * JaamSim Discrete Event Simulation
 * Copyright (C) 2013 Ausenco Engineering Canada Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jaamsim.ProbabilityDistributions;

import com.jaamsim.input.Keyword;
import com.jaamsim.input.ValueInput;
import com.jaamsim.rng.MRG1999a;
import com.jaamsim.units.DimensionlessUnit;
import com.jaamsim.units.Unit;
import com.jaamsim.units.UserSpecifiedUnit;

/**
 * LogNormal Distribution.
 * Adapted from A.M. Law, "Simulation Modelling and Analysis, 4th Edition", page 454.
 * Polar Method, Marsaglia and Bray (1964) is used to calculate the normal distribution
 */
public class LogNormalDistribution extends Distribution {

	@Keyword(description = "The scale parameter for the Log-Normal distribution.",
	         exampleList = {"3.0 h"})
	private final ValueInput scaleInput;

	@Keyword(description = "The mean of the dimensionless normal distribution (not the mean of the lognormal).",
	         exampleList = {"5.0"})
	private final ValueInput normalMeanInput;

	@Keyword(description = "The standard deviation of the dimensionless normal distribution (not the standard deviation of the lognormal).",
	         exampleList = {"2.0"})
	private final ValueInput normalStandardDeviationInput;

	private final MRG1999a rng1 = new MRG1999a();
	private final MRG1999a rng2 = new MRG1999a();

	{
		minValueInput.setDefaultValue(0.0);

		scaleInput = new ValueInput("Scale", "Key Inputs", 1.0d);
		scaleInput.setValidRange( 0.0, Double.POSITIVE_INFINITY);
		scaleInput.setUnitType( UserSpecifiedUnit.class );
		this.addInput(scaleInput);

		normalMeanInput = new ValueInput("NormalMean", "Key Inputs", 0.0d);
		normalMeanInput.setUnitType(DimensionlessUnit.class);
		this.addInput(normalMeanInput);

		normalStandardDeviationInput = new ValueInput("NormalStandardDeviation", "Key Inputs", 1.0d);
		normalStandardDeviationInput.setUnitType(DimensionlessUnit.class);
		normalStandardDeviationInput.setValidRange(0.0d, Double.POSITIVE_INFINITY);
		this.addInput(normalStandardDeviationInput);
	}

	public LogNormalDistribution() {}

	@Override
	public void earlyInit() {
		super.earlyInit();

		rng1.setSeedStream(getStreamNumber()    , getSubstreamNumber());
		rng2.setSeedStream(getStreamNumber() + 1, getSubstreamNumber());
	}

	@Override
	protected void setUnitType(Class<? extends Unit> specified) {
		super.setUnitType(specified);
		scaleInput.setUnitType(specified);
	}

	@Override
	protected double getNextSample() {

		// Loop until we have a random x-y coordinate in the unit circle
		double w, v1, v2, sample;
		do {
			v1 = 2.0 * rng1.nextUniform() - 1.0;
			v2 = 2.0 * rng2.nextUniform() - 1.0;
			w = ( v1 * v1 ) + ( v2 * v2 );
		} while( w > 1.0 || w == 0.0 );

		// Calculate the normalised random sample
		// (normally distributed with mode = 0 and standard deviation = 1)
		sample = v1 * Math.sqrt( -2.0 * Math.log( w ) / w );

		// Adjust for the desired mode and standard deviation
		sample = normalMeanInput.getValue() + ( sample * normalStandardDeviationInput.getValue() );

		// Convert to lognormal
		return scaleInput.getValue() * Math.exp( sample );
	}

	@Override
	protected double getMeanValue() {
		double sd = normalStandardDeviationInput.getValue();
		return scaleInput.getValue() * Math.exp( normalMeanInput.getValue() + sd*sd/2.0 );
	}

	@Override
	protected double getStandardDeviation() {
		double sd = normalStandardDeviationInput.getValue();
		return this.getMeanValue() * Math.sqrt( Math.exp( sd*sd ) - 1.0 );
	}
}
