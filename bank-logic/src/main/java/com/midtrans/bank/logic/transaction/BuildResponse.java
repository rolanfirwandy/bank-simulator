package com.midtrans.bank.logic.transaction;

import com.midtrans.bank.core.model.BatchTxn;
import com.midtrans.bank.core.model.SettlementTxn;
import com.midtrans.bank.core.model.Transaction;
import com.midtrans.bank.core.model.VoidTxn;
import com.midtrans.bank.core.transaction.BankTxnSupport;
import org.jpos.iso.ISODate;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.ISO87BPackager;
import org.jpos.transaction.AbortParticipant;
import org.jpos.transaction.Context;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: shaddiqa
 * Date: 9/9/13
 * Time: 1:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class BuildResponse extends BankTxnSupport implements AbortParticipant {
    @Override
    protected int doPrepare(long id, Context ctx) throws Exception {
        return buildResponse(ctx, "00");
    }

    @Override
    protected int doPrepareForAbort(long id, Context ctx) throws Exception {
        String rCode = ctx.getString("RC");

        return buildResponse(ctx, rCode);
    }

    private int buildResponse(Context ctx, String rCode) throws ISOException {
        String mti = ctx.getString(MTI);
        String pCode = ctx.getString(PCODE);
        Long amount = (Long) ctx.get(AMOUNT);
        Integer traceNumber = (Integer) ctx.get(TRACE_NUMBER);
        Date txnTime = (Date) ctx.get(TXN_TIME);
        String nii = ctx.getString(NII);
        String refNo = ctx.getString(REFERENCE_NUMBER);
        String authId = ctx.getString(AUTHORIZATION_ID);
        String tid = ctx.getString(TID);

        String command = ctx.getString(COMMAND);
        if("Sale".equals(command)) {
            Transaction txn = (Transaction) ctx.get(TXN);
            txn.setResponseCode(rCode);

            ctx.put(TXN, txn);
        } else if("Void".equals(command)) {
            VoidTxn voidTxn = (VoidTxn) ctx.get(VOID_TXN);
            voidTxn.setResponseCode(rCode);

            ctx.put(VOID_TXN, voidTxn);
        } else if("Settlement".equals(command) || "SettlementTrailer".equals(command)) {
            SettlementTxn settlementTxn = (SettlementTxn) ctx.get(SETTLE_TXN);
            settlementTxn.setResponseCode(rCode);

            ctx.put(SETTLE_TXN, settlementTxn);
        } else if("BatchUpload".equals(command)) {
            BatchTxn batchTxn = (BatchTxn) ctx.get(BATCH_TXN);
            batchTxn.setResponseCode(rCode);

            ctx.put(BATCH_TXN, batchTxn);
        }

        ctx.put(RCODE, rCode);
        ctx.put(RESPONSE, createResponse(responseMTI(mti), pCode, amount, traceNumber, txnTime, nii, refNo, authId, rCode, tid));

        return PREPARED | NO_JOIN;
    }

    private ISOMsg createResponse(String mti, String pCode, Long amount, Integer traceNumber, Date txnTime, String nii, String refNo, String authId, String rCode, String tid) throws ISOException {
        ISOMsg response = new ISOMsg();

        response.setPackager(new ISO87BPackager());
        response.setMTI(mti);
        response.set(3, pCode);
        if(amount != null) {
            response.set(4, String.valueOf(amount));
        }
        response.set(11, String.valueOf(traceNumber));
        response.set(12, ISODate.getTime(txnTime));
        response.set(13, ISODate.getDate(txnTime));
        response.set(24, nii);
        response.set(37, refNo);
        if(authId != null) {
            response.set(38, authId);
        }
        response.set(39, rCode);
        response.set(41, tid);

        return response;
    }
}
