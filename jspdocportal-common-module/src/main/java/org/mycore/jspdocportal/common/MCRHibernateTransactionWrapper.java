/* 
 * $Revision: 34285 $ $Date: 2016-01-07 14:05:50 +0100 (Do, 07 Jan 2016) $
 *
 * This file is part of ** M y C o R e **
 * Visit our homepage at http://www.mycore.de/ for details.
 *
 * This program is free software; you can use it, redistribute it
 * and / or modify it under the terms of the GNU General Public License
 * (GPL) as published by the Free Software Foundation; either version 2
 * of the License or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program, normally in the file license.txt.
 * If not, write to the Free Software Foundation Inc.,
 * 59 Temple Place - Suite 330, Boston, MA  02111-1307 USA
 *
 */
package org.mycore.jspdocportal.common;

import org.mycore.common.MCRException;
import org.mycore.common.MCRTransactionHelper;

/**
 * This AutoClosable can be used to begin and commit
 * a JPA (Hibernate) transaction in the current MyCoRe Session
 * 
 * @author Robert Stephan
 */
public class MCRHibernateTransactionWrapper implements AutoCloseable {
    private boolean responsibleForTransaction = false;

    public MCRHibernateTransactionWrapper() {
        /* 
        EntityTransaction et = MCREntityManagerProvider.getCurrentEntityManager().getTransaction();
        if(!et.isActive()) {
            responsibleForTransaction = true;
            et.begin();
        }
        */
        if (!MCRTransactionHelper.isTransactionActive()) {
            responsibleForTransaction = true;
            MCRTransactionHelper.beginTransaction();
        }
    }

    @Override
    public void close() throws MCRException {
        if (responsibleForTransaction) {
            responsibleForTransaction = false;
            /*    
            EntityTransaction et = MCREntityManagerProvider.getCurrentEntityManager().getTransaction();
            et.commit();
            */
            MCRTransactionHelper.commitTransaction();
        }
    }
}
