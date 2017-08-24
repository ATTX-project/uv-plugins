/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uh.hulib.attx.wc.uv.common;

/**
 *
 * @author jkesanie
 */
public interface MessagingClient {
    
    public void sendProvMessage(String content) throws Exception;    
    public String sendSyncServiceMessage(String content, String targetQueue, int timeout) throws Exception;
    
}
