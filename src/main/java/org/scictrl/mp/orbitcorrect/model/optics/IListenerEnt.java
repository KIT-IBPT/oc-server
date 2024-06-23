package org.scictrl.mp.orbitcorrect.model.optics;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.scictrl.mp.orbitcorrect.ActionReportEvent;
import org.scictrl.mp.orbitcorrect.ConnectionReportEvent;
import org.scictrl.mp.orbitcorrect.DataBushEvent;
import org.scictrl.mp.orbitcorrect.IActionReportListener;
import org.scictrl.mp.orbitcorrect.IConnectionListener;
import org.scictrl.mp.orbitcorrect.IDataBushListener;

/**
 *
 */
class IListenerEnt implements IDataBushListener, IActionReportListener, IConnectionListener {

	static final Logger logger = LogManager.getLogger(IListenerEnt.class);

	private DBLink dbFirst;
	private ARLink arFirst;
	private ConLink conFirst;

	private class DBLink {
		DBLink next=null;
		IDataBushListener listen;
		DBLink(IDataBushListener l) {
			super();
			listen= l;
		}
	}

	private class ARLink {
		ARLink next=null;
		IActionReportListener listen;
		ARLink(IActionReportListener l) {
			super();
			listen= l;
		}
	}

	private class ConLink {
		ConLink next=null;
		IConnectionListener listen;
		ConLink(IConnectionListener l) {
			super();
			listen= l;
		}
	}
/**
 * <p>addARListener.</p>
 *
 * @param l a {@link org.scictrl.mp.orbitcorrect.IActionReportListener} object
 */
public synchronized void addARListener(IActionReportListener l) {
	if (arFirst==null) arFirst= new ARLink(l);
	else {
		ARLink a= arFirst;
		while (a.next!=null) a= a.next;
		a.next= new ARLink(l);
	}
}
/**
 * <p>addConListener.</p>
 *
 * @param l a {@link org.scictrl.mp.orbitcorrect.IConnectionListener} object
 */
public synchronized void addConListener(IConnectionListener l) {
	if (conFirst==null) conFirst= new ConLink(l);
	else {
		ConLink a= conFirst;
		while (a.next!=null) a= a.next;
		a.next= new ConLink(l);
	}
}
/**
 * <p>addDBListener.</p>
 *
 * @param l a {@link org.scictrl.mp.orbitcorrect.IDataBushListener} object
 */
public synchronized void addDBListener(IDataBushListener l) {
	if (dbFirst==null) dbFirst= new DBLink(l);
	else {
		DBLink a= dbFirst;
		while (a.next!=null) a= a.next;
		a.next= new DBLink(l);
	}
}
/** {@inheritDoc} */
@Override
public void applyPerformed(ActionReportEvent e) {
	ARLink l= incARLink(null);
	while (l!=null) {
		try{
			l.listen.applyPerformed(e);
		} catch (Throwable t) {
			logger.warn("DB> Error dispaching event \"applyPerformed\", do NOT blame DataBush!", t);
		}
		l=incARLink(l);
	}
}
/** {@inheritDoc} */
@Override
public void beamChanged(DataBushEvent e) {
	DBLink l= incDBLink(null);
	while (l!=null) {
		try{
			l.listen.beamChanged(e);
		} catch (Throwable t) {
			logger.warn("DB> Error dispaching event \"beamChanged\", do NOT blame DataBush!", t);
		}
		l=incDBLink(l);
	}
}
/** {@inheritDoc} */
@Override
public void deviceConnected(ConnectionReportEvent e) {
	ConLink l= incConLink(null);
	while (l!=null) {
		try{
			l.listen.deviceConnected(e);
		} catch (Throwable t) {
			logger.warn("DB> Error dispaching event \"deviceConnected\", do NOT blame DataBush!", t);
		}
		l=incConLink(l);
	}
}
/** {@inheritDoc} */
@Override
public void deviceDisconnected(ConnectionReportEvent e) {
	ConLink l= incConLink(null);
	while (l!=null) {
		try{
			l.listen.deviceDisconnected(e);
		} catch (Throwable t) {
			logger.warn("DB> Error dispaching event \"deviceDisconnected\", do NOT blame DataBush!", t);
		}
		l=incConLink(l);
	}
}
/** {@inheritDoc} */
@Override
public void fieldChanged(DataBushEvent e) {
	DBLink l= incDBLink(null);
	while (l!=null) {
		try{
			l.listen.fieldChanged(e);
		} catch (Throwable t) {
			logger.warn("DB> Error dispaching event \"fieldChanged\", do NOT blame DataBush!", t);
		}
		l=incDBLink(l);
	}
}
/**
 *
 * @return ARLink
 * @param ar ARLink
 */
private synchronized ARLink incARLink(ARLink ar) {
	if (ar==null) return arFirst;
	return ar.next;
}
/**
 *
 * @return ARLink
 * @param ar ARLink
 */
private synchronized ConLink incConLink(ConLink con) {
	if (con==null) return conFirst;
	return con.next;
}
/**
 *
 * @return ARLink
 * @param ar ARLink
 */
private synchronized DBLink incDBLink(DBLink db) {
	if (db==null) return dbFirst;
	return db.next;
}
/** {@inheritDoc} */
@Override
public void inconsistentData(DataBushEvent e) {
	DBLink l= incDBLink(null);
	while (l!=null) {
		try{
			l.listen.inconsistentData(e);
		} catch (Throwable t) {
			logger.warn("DB> Error dispaching event \"inconsistentData\", do NOT blame DataBush!", t);
		}
		l=incDBLink(l);
	}
}
/** {@inheritDoc} */
@Override
public void machineFunctionsChanged(DataBushEvent e) {
	DBLink l= incDBLink(null);
	while (l!=null) {
		try{
			l.listen.machineFunctionsChanged(e);
		} catch (Throwable t) {
			logger.warn("DB> Error dispaching event \"machineFunctionsChanged\", do NOT blame DataBush!", t);
		}
		l=incDBLink(l);
	}
}
/**
 * <p>removeARListener.</p>
 *
 * @param l a {@link org.scictrl.mp.orbitcorrect.IActionReportListener} object
 */
public synchronized void removeARListener(IActionReportListener l) {
	if (arFirst==null) return;
	if (arFirst.listen.equals(l)) {
		arFirst= arFirst.next;
		return;
	}
	ARLink a= arFirst;
	ARLink b= a.next;
	while ((!b.listen.equals(l))&&(b.next!=null)) {
		a= b;
		b= b.next;
	}
	if (b.listen.equals(l)) a.next= b.next;
}
/**
 * <p>removeConListener.</p>
 *
 * @param l a {@link org.scictrl.mp.orbitcorrect.IConnectionListener} object
 */
public synchronized void removeConListener(IConnectionListener l) {
	if (conFirst==null) return;
	if (conFirst.listen.equals(l)) {
		conFirst= conFirst.next;
		return;
	}
	ConLink a= conFirst;
	ConLink b= a.next;
	while ((!b.listen.equals(l))&&(b.next!=null)) {
		a= b;
		b= b.next;
	}
	if (b.listen.equals(l)) a.next= b.next;
}
/**
 * <p>removeDBListener.</p>
 *
 * @param l a {@link org.scictrl.mp.orbitcorrect.IDataBushListener} object
 */
public synchronized void removeDBListener(IDataBushListener l) {
	if (dbFirst==null) return;
	if (dbFirst.listen.equals(l)) {
		dbFirst= dbFirst.next;
		return;
	}
	DBLink a= dbFirst;
	DBLink b= a.next;
	while ((!b.listen.equals(l))&&(b.next!=null)) {
		a= b;
		b= b.next;
	}
	if (b.listen.equals(l)) a.next= b.next;
}
/** {@inheritDoc} */
@Override
public void rfChanged(DataBushEvent e) {
	DBLink l= incDBLink(null);
	while (l!=null) {
		try{
			l.listen.rfChanged(e);
		} catch (Throwable t) {
			logger.warn("DB> Error dispaching event \"rfChanged\", do NOT blame DataBush!", t);
		}
		l=incDBLink(l);
	}
}
/** {@inheritDoc} */
@Override
public void statusChanged(DataBushEvent e) {
	DBLink l= incDBLink(null);
	while (l!=null) {
		try{
			l.listen.statusChanged(e);
		} catch (Throwable t) {
			logger.warn("DB> Error dispaching event \"statusChanged\", do NOT blame DataBush!", t);
		}
		l=incDBLink(l);
	}
}
/** {@inheritDoc} */
@Override
public void updatePerformed(ActionReportEvent e) {
	ARLink l= incARLink(null);
	while (l!=null) {
		try{
			l.listen.updatePerformed(e);
		} catch (Throwable t) {
			logger.warn("DB> Error dispaching event \"updatePerformed\", do NOT blame DataBush!", t);
		}
		l=incARLink(l);
	}
}
}
