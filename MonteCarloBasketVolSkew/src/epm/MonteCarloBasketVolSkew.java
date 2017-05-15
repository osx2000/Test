package epm;

import com.olf.embedded.valuation.AbstractInstrumentModel;
import com.olf.embedded.valuation.InstrumentModel.VarianceLookup;
import com.olf.openrisk.application.EnumDebugLevel;
import com.olf.openrisk.application.Session;
import com.olf.openrisk.market.Correlation;
import com.olf.openrisk.market.EnumElementType;
import com.olf.openrisk.market.EnumVolKeyTypes;
import com.olf.openrisk.market.Index;
import com.olf.openrisk.market.Market;
import com.olf.openrisk.market.VarianceData;
import com.olf.openrisk.market.VarianceKey;
import com.olf.openrisk.market.VarianceKeys;
import com.olf.openrisk.market.Volatility;
import com.olf.openrisk.market.VolatilityLayer;
import com.olf.openrisk.staticdata.EnumFieldType;
import com.olf.openrisk.staticdata.EnumReferenceObject;
import com.olf.openrisk.staticdata.ReferenceObject;
import com.olf.openrisk.staticdata.StaticDataFactory;
import com.olf.openrisk.table.Table;
import com.olf.openrisk.trading.EnumInsType;
import com.olf.openrisk.trading.EnumLegFieldId;
import com.olf.openrisk.trading.EnumProfileFieldId;
import com.olf.openrisk.trading.Instrument;
import com.olf.openrisk.trading.InstrumentRegistrar;
import com.olf.openrisk.trading.Leg;
import com.olf.openrisk.trading.Legs;
import com.olf.openrisk.trading.Options;
import com.olf.openrisk.trading.Profile;
import com.olf.openrisk.trading.Profiles;

import com.olf.openrisk.trading.TradingFactory;
import com.olf.openrisk.trading.Transaction;

import com.olf.embedded.application.ScriptCategory;
import com.olf.embedded.generic.ExtensionAttributes;

import java.util.ArrayList;

import com.olf.embedded.application.EnumScriptCategory;

/* DB Version : V14.1
 * Author : Zheng Yang
 * Activity :
 * Feb 16th,2017,created POC version
 *  */
@ScriptCategory({ EnumScriptCategory.InstrumentModel })
public class MonteCarloBasketVolSkew extends AbstractInstrumentModel {
		private Session _session;
		private Transaction _tran;
		private Instrument _ins;
		private TradingFactory tf;
		private double[] basketStrikes;
		private double[] initialPrices;
		private double[] weightings;
		private boolean initColNums = false;
		private Market _mkt;
		private double[] volLookups;
		private VarianceData[] _varianceData;
		private int flag=0;
		private double w1,w2,strike,notionalAmount=0.0;
		
		@Override
		public void register(Session session, InstrumentRegistrar registrar) {
			// TODO Auto-generated method stub
			registrar.registerInstrumentType(EnumInsType.EquityOtcOptionBasketCall);
			registrar.registerInstrumentType(EnumInsType.EquityOtcOptionBasketPut);
		}

		@Override
		public void initialize(Session session, ExtensionAttributes attributes, Instrument instrument) {
			if(session.getDebug().getDebugLevel().getValue() >= EnumDebugLevel.Medium.getValue()){
				session.getDebug().printLine("Monte Carlo Basket Vol Skew compiled for build V14.1 on Feb, 2017");
	        }
			super.initialize(session, attributes, instrument);
			try {
				/*initialize variables*/
				_session = session;
				_ins = instrument;
				
				_mkt=_session.getMarket();
				_tran=_ins.getTransaction();
				basketStrikes= new double[_ins.getLegCount()];
				initialPrices= new double[_ins.getLegCount()];
				weightings=new double[_ins.getLegCount()];
				
				
				for(int i=1;i<_ins.getLegCount();i++)
					
				{//Retrieve basket strikes from the deal 
					if(_ins.getLeg(i).getField("Basket Strike")== null)
					{	
						throw new RuntimeException( "Please have 'Basket Stike'field created correctly in Instrument Builder.");   
					}
					else if (_ins.getLeg(i).getField("Basket Strike").getDataType()!=EnumFieldType.Double)	
					{
						throw new IllegalArgumentException( "'Basket Strike' field must utilize 'Double' as Data Type.");   
					}
				    else
					{	
						basketStrikes[i]=_ins.getLeg(i).getField("Basket Strike").getValueAsDouble();
						//Retrieve initial Stike,Weighting,Notional Amount,to be enhanced
						initialPrices[i]=_ins.getLeg(i).getField("Initial Price").getValueAsDouble();
						weightings[i]=_ins.getLeg(i).getField("Weighting").getValueAsDouble();
						notionalAmount=26508.10;//to be enhanced
						_session.getDebug().printLine("********Strike:"+basketStrikes[i]);
					}	 
				}
				
				//compute adjusted weights and (total) strike, to be enhanced
				w1=100*notionalAmount*weightings[1]/initialPrices[1];//to include notional
				w2=100*notionalAmount*weightings[2]/initialPrices[2];
				strike=w1*initialPrices[1]+w2*initialPrices[2];
				_session.getDebug().printLine("****************Total Strike:"+strike);
				
				//set the weights back to index percent fields and the strike value to the strike field
				//_tran.getLeg(1).setValue(EnumLegFieldId.IndexPercentage, w1);
				_ins.getLeg(1).setValue(EnumLegFieldId.IndexPercentage, w1);
				_ins.getLeg(2).setValue(EnumLegFieldId.IndexPercentage, w2);
				_ins.getLeg(0).setValue(EnumLegFieldId.Strike, strike);				

			}
	        catch (Exception e)
			{
				e.printStackTrace(); 	
			}
		}

	
/*		
	@Override
		public void modifyVarianceData(Profile profile, VarianceLookup varianceLookup, int field, double[] data) {
			// TODO Auto-generated method stub
			
			super.modifyVarianceData(profile, varianceLookup, field, data);
			_session.getDebug().printLine("Test2");
			
			VarianceData varianceData = varianceLookup.getVarianceData();
		    Table varData = varianceData.getInputTable().asTable();
		}*/

	@Override
		public void modifyVarianceKeys(Profile profile, VarianceLookup varianceLookup) {
			// TODO Auto-generated method stub
			//super.modifyVarianceKeys(profile, varianceLookup);
			_session.getDebug().printLine("Test3");
			
			VarianceKey key=varianceLookup.getVarianceKeys(0).getItem(1);
			if (key.getKeyType()==EnumVolKeyTypes.Strike)
				{
				key.setValue(basketStrikes[1]);
				}
			//Table keyTable = key.getKeyType();
			//this.lookupVarianceData(_varianceData[flag]);
				//this.lookupVarianceData(_varianceData[flag+1]);
			//	flag++;
			
			//VarianceKeys keys=varianceLookup.getVarianceKeys(0);
		
			//VarianceLookup[] varianceLookup=new VarianceLookup[1];
			//VarianceData varianceData_aux=new VarianceData
				//	varianceLookup_aux[0].getVarianceData();
			
			//varianceData_aux.setInputFromTable(_varianceData[0].getInputTable());
		
			//VarianceData varianceData = varianceLookup.getVarianceData();
			//varianceData.setInputFromTable(_varianceData[1].getInputTable());
			
			//Table varData = varianceData_aux..getInputTable().asTable();
			//_session.getDebug().printLine(key.getKeyType().getName());
			_session.getDebug().viewTable(varianceLookup.getVarianceKeys(0).asTable());
		}
		
		
		//must have for V14.1,incompatible with modifyVarianceKeys()
/*	@Override
	    public void lookupVarianceData(VarianceData varianceData) {
			 //super.lookupVarianceData(varianceData);
			_session.getDebug().printLine("Test2");
					//Modify volatility lookup only
					//if the vols haven't saved 
					if(varianceData.getElementType().Volatility != null && volLookups == null)
					{
						volLookups= new double[_ins.getLegCount()-1];//one vol per basket item
						//_session.getDebug().printLine("volLookup: "+lookupVol(basketStrikes[1],42954,2204.71,vols.getId()));
						volLookups[0]=100*lookupVol(basketStrikes[1],42954,2219.83,varianceData.getId());//to be enhanced
						volLookups[1]=100*lookupVol(basketStrikes[2],42954,3011.62,20086);//to be enhanced
					}
				}
*/
		private double lookupVol(double basketStrike, int jExpDate, double mktStrike,int volId) {
			
			//expecting volatility structure with 3 keys: expiration date, strike, market strike
			VarianceData vol = _mkt.getVarianceData(volId);
			VarianceKeys keys=vol.createLookupKeys();
			double volLookup=0.0;
				
			for (VarianceKey key : keys){
				
				switch (key.getKeyType()) {
					case OptExpDate:
						key.setValue(jExpDate);
						break;
					case Strike:
						key.setValue(basketStrike);
						break;
					case MktStrike:
						key.setValue(mktStrike);
						break;
				}	
			}
			volLookup = vol.getOutputValue(keys);
			
			_session.getDebug().printLine("volLookup : " +volLookup);
				
			return volLookup;
		}
		//VarianceData set needs to be expanded to include correlation set
		/*@Override
		public VarianceData[] getRequiredVarianceData() {
			// TODO Auto-generated method stub
			_session.getDebug().printLine("Test1");
			 _varianceData =super.getRequiredVarianceData();
	
			if (_varianceData != null) 
				
			{   
				_varianceData=new VarianceData[3];//for correlation
				int len = _varianceData.length;
				for ( VarianceData vardata : _varianceData)
				{
					//_session.getDebug().viewTable(vardata.getInputTable().asTable());
				}
			
			}	
				
			return _varianceData;
		}*/

		/*@Override
		public double calcPresentValue(Leg paramLeg, Table details) {
			// TODO Auto-generated method stub
			double pv = 0;
			double cflow = 0;
			double inscflow = 0;
			int basketItemId=0;
			_session.getDebug().viewTable(details);
			super.calcPresentValue(paramLeg, details);
			
			_session.getDebug().viewTable(details);
			Table modelInputs=this.getModelInputsTable(paramLeg.getProfile(0));
			modelInputs.getTable("item", 0).setValue("vol", 0, volLookups[0]);//to be enhanced
			modelInputs.getTable("item", 0).setValue("vol", 1, volLookups[1]);//to be enhanced
			
			pv=this.calcModel(paramLeg.getProfile(0), modelInputs);//to add cashflows
			
			//to modify pricing details
			//_session.getDebug().viewTable(modelInputs.getTable("item", 0));
			_session.getDebug().printLine("new PV: "+ pv);
			return pv;
		}*/
		/*@Override
		public void dispose() {
			
			basketStrikes = null; 
			initialPrices = null;
			weightings=null;
			super.dispose();
		
		}*/
}
