package org.scictrl.mp.orbitcorrect.epics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.scictrl.csshell.RemoteException;
import org.scictrl.csshell.Response;
import org.scictrl.csshell.ResponseEvent;
import org.scictrl.csshell.ResponseListener;
import org.scictrl.csshell.epics.EPICSConnection;
import org.scictrl.csshell.epics.EPICSConnector;
import org.scictrl.mp.orbitcorrect.ControlSystemException;
/**
 *
 *
 * @author:
 */
class MyCallback implements ResponseListener<EPICSConnector> {
	private boolean dead=false;
	public ArrayList<String> done= new ArrayList<>();
	public HashMap<String, EPICSConnection<Double>> send= new HashMap<>();
	public HashMap<String,Times> times= new HashMap<>(300);
	public int count=0;
	long last=0;


	class Times {
		int retry=0;
		double value=0.0;
		long start=0;
		long stop=0;
		Response<EPICSConnector> event;
	public Times(double d) {
		start= System.currentTimeMillis();
		value=d;
	}
	@Override
	public String toString() {
		return "("+start+","+(MyCallback.this.start-start)+","+(stop-start)+")";

	}
	}
	private org.scictrl.mp.orbitcorrect.ControlSystemException exception;
	private Long start;
	/**
	 * MyCallback constructor comment.
	 */
	public MyCallback() {
		super();
	}

	/**
	 * <p>sendRequest.</p>
	 *
	 * @param p a {@link org.scictrl.csshell.epics.EPICSConnection} object
	 * @param value a double
	 * @throws org.scictrl.csshell.RemoteException if any.
	 */
	public synchronized void sendRequest(EPICSConnection<Double> p, double value) throws RemoteException {
		if (start!=null) start=System.currentTimeMillis();
		send.put(p.getName(),p);
		times.put(p.getName(),new Times(value));
		last= System.currentTimeMillis();
		count++;
		p.setValue(value, this);
	}

	/** {@inheritDoc} */
	@Override
	public void responseReceived(ResponseEvent<EPICSConnector> re) {
		if (re.isLast()) {
			if (re.getResponse().isSuccess()) {
				callbackDone(re.getResponse());
			} else {
				timeout(re.getResponse());
			}
		}
	}

	/**
	 * <p>callbackDone.</p>
	 *
	 * @param re a {@link org.scictrl.csshell.Response} object
	 */
	public void callbackDone(Response<EPICSConnector> re) {
		//if (re.getRequest().Source() instanceof si.ijs.anka.abeans.datatypes.RWDoubleProperty) {
	//		((si.ijs.anka.abeans.datatypes.RWDoubleProperty)e.getSource()).removeCallbackDoneListener(this);
			Object o= times.get(re.getConnection().getName());
			if (o!=null) {
				Times t= (Times)o;
				t.stop= System.currentTimeMillis();
				t.event= re;
				last= System.currentTimeMillis();
			} else {
				System.out.println("AFastSet: unregistered callback '"+(re.getConnection().getName())+"'");
			}
			if (dead) {
				System.out.println("AFastSet: callback after timeout: "+(re.getConnection().getName())+times.get(re.getConnection().getName()));
				return;
			}
			synchronized(this) {
				done.add(re.getConnection().getName());
				if (--count==0) this.notify();
			}
		//}
	}

	/**
	 * Called on all the listeners when one of the callbacks experiences the timeout.
	 *
	 * @param re a {@link org.scictrl.csshell.Response} object
	 */
	public void timeout(Response<EPICSConnector> re) {
		Object o= times.get(re.getConnection().getName());
		if (o!=null) {
			Times t= (Times)o;

			if (t.retry>10) {
				System.out.println("AFastSet: timeout for '"+re.getConnection().getName()+"', after 10 retries!");
				return;
			}

			t.retry++;

			System.out.println("AFastSet: timeout for '"+re.getConnection().getName()+"', retrying set for "+t.retry+" time !");

			try {
				send.get(re.getConnection().getName()).setValue(t.value,this);
			} catch (RemoteException ex) {
				exception= new ControlSystemException(ex);
			}

			last=System.currentTimeMillis();

		} else {
			System.out.println("AFastSet: timeout for unregistered callback '"+re.getConnection().getName()+"'");
		}
		if (dead) {
			System.out.println("AFastSet: timeout event after end: "+re.getConnection().getName()+times.get(re.getConnection().getName()));
			return;
		}

	}
	/**
	 * <p>waitForCallback.</p>
	 *
	 * @throws org.scictrl.mp.orbitcorrect.ControlSystemException if any.
	 */
	public synchronized void waitForCallback() throws org.scictrl.mp.orbitcorrect.ControlSystemException {
		if (count!=0) {
			do {
				try {
					wait(1000);
				} catch(InterruptedException ie) {
					System.out.println("DB> --- UNCAUGHT EXCEPTION ---");
					ie.printStackTrace();
				}
			} while((count>0)&&(System.currentTimeMillis()-last < 30000)&&(exception==null));

			dead=true;

			if (count!=0) {
				//StringBuffer sb= new StringBuffer();
				//sb.append("Asynchronous set send to: [");
				//if (send.size()>0) sb.append(send.get(0));
				//for (int l=1;l<send.size(); l++) sb.append(","+send.get(l));

				//sb.append("]\nReturned: [");
				if (done.size()>0) {
					//sb.append(done.get(0));
					//sb.append(times.get(done.get(0)));
					send.remove(done.get(0));
				}
				for (int l=1;l<done.size(); l++) {
					//sb.append(","+done.get(l));
					//.append(times.get(done.get(l)));
					send.remove(done.get(l));
				}
				//sb.append("]\n");

				//System.out.println(sb.toString());

				StringBuffer sb= new StringBuffer();
				Iterator<String> it= send.keySet().iterator();
				if (it.hasNext()) {
					sb.append("'");
					sb.append(it.next());
					sb.append("'");
				}
				while (it.hasNext()) {
					sb.append(",'");
					sb.append(it.next());
					sb.append("'");
				}

				done.clear();
				send.clear();

				if(exception!=null) {
					System.out.println("Exception while retrying: "+exception.getIntercepted());
					throw exception;
				}
				throw new ControlSystemException("Timeout fast setting, "+count+" callbacks not returned for "+sb.toString()+"!");
			}
		}

		dead=true;

		/*StringBuffer sb= new StringBuffer();
		sb.append("Asynchronous set successful for: [");
		if (done.size()>0) {
			sb.append(done.get(0));
			sb.append(times.get(done.get(0)));
		}
		for (int l=1;l<done.size(); l++) {
			sb.append(","+done.get(l));
			sb.append(times.get(done.get(l)));
		}
		sb.append("]");

		System.out.println(sb.toString());*/

		times.clear();
		done.clear();
		send.clear();

	}

}
