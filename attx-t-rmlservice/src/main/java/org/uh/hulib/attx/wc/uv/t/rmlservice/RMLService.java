/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uh.hulib.attx.wc.uv.t.rmlservice;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.DataUnitUtils;
import eu.unifiedviews.helpers.dataunit.files.FilesHelper;
import eu.unifiedviews.helpers.dataunit.rdf.RdfDataUnitUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;

import eu.unifiedviews.helpers.dpu.extension.rdf.simple.WritableSimpleRdf;
import eu.unifiedviews.helpers.dpu.rdf.EntityBuilder;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.openrdf.model.ValueFactory;
import org.uh.hulib.attx.wc.uv.common.ActiveMQClient;
import org.uh.hulib.attx.wc.uv.common.MessagingClient;
import org.uh.hulib.attx.wc.uv.common.pojos.RMLServiceRequest;
import org.uh.hulib.attx.wc.uv.common.pojos.RMLServiceResponse;
import org.uh.hulib.attx.wc.uv.common.pojos.prov.Activity;
import org.uh.hulib.attx.wc.uv.common.pojos.prov.Agent;
import org.uh.hulib.attx.wc.uv.common.pojos.prov.Communication;
import org.uh.hulib.attx.wc.uv.common.pojos.prov.Context;
import org.uh.hulib.attx.wc.uv.common.pojos.prov.DataProperty;
import org.uh.hulib.attx.wc.uv.common.pojos.prov.Provenance;

/**
 * Main data processing unit class.
 *
 * @author Unknown
 */
@DPU.AsTransformer
public class RMLService extends AbstractDpu<RMLServiceConfig_V1> {

    public static final String datasetURISymbolicName = "datasetURI";

    private static final Logger log = LoggerFactory.getLogger(RMLService.class);

    @DataUnit.AsInput(name = "fileInput", optional = true)
    public FilesDataUnit filesInput;

    @DataUnit.AsOutput(name = "datasetURI")
    public WritableRDFDataUnit datasetURI;

    @ExtensionInitializer.Init(param = "datasetURI")
    public WritableSimpleRdf rdfData;

    public RMLService() {
        super(RMLServiceVaadinDialog.class, ConfigHistory.noHistory(RMLServiceConfig_V1.class));
    }

    @Override
    protected void innerExecute() throws DPUException {

        ContextUtils.sendShortInfo(ctx, "RMLService.message");
        try {
            Set<FilesDataUnit.Entry> fileEntries = FilesHelper.getFiles(filesInput);
            if (fileEntries.size() > 0) {
                System.out.println("Input:");
                System.out.println("- File:" + fileEntries.iterator().next().getFileURIString());
                System.out.println("- RML conf:");
                System.out.println(config.getConfiguration());

                MessagingClient mq = new ActiveMQClient("tcp://mom:61616", "provenance.inbox");
                ObjectMapper mapper = new ObjectMapper();
                Provenance prov = getProv();                

                try {
                    RMLServiceRequest request = new RMLServiceRequest();
                    request.setMapping(config.getConfiguration());
                    request.setSourceData(FileUtils.readFileToString(new File(new URI(fileEntries.iterator().next().getFileURIString())), "UTF-8"));
                    RMLServiceResponse response = mapper.readValue(mq.sendSyncServiceMessage(mapper.writeValueAsString(request), "attx.RMLService", 10000), RMLServiceResponse.class);
                    System.out.println(response.getTransformedDatasetURL());
                    prov.getActivity().setStatus("SUCCESS");
                    mq.sendProvMessage(mapper.writeValueAsString(prov));

                    final ValueFactory vf = rdfData.getValueFactory();
                    // entry is the graph 
                    final RDFDataUnit.Entry entry = RdfDataUnitUtils.addGraph(
                            datasetURI,
                            DataUnitUtils.generateSymbolicName(RMLService.class));

                    final EntityBuilder datasetUriEntity = new EntityBuilder(vf.createURI("http://hulib.helsinki.fi/attx/uv/dpu/RMLService"), vf);
                    datasetUriEntity.property(vf.createURI("http://hulib.helsinki.fi/attx/uv/dpu/fileURI"), vf.createURI(response.getTransformedDatasetURL()));

                    rdfData.setOutput(entry);
                    rdfData.add(datasetUriEntity.asStatements());

                } catch (Exception ex) {
                    ex.printStackTrace();
                    prov.getActivity().setStatus("FAILED");
                    mq.sendProvMessage(mapper.writeValueAsString(prov));
                    ContextUtils.sendError(ctx, "Error occured.", ex.getMessage());
                }

            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Provenance getProv() {
        long stepID = ctx.getExecMasterContext().getDpuContext().getDpuInstanceId();
        long workflowID = ctx.getExecMasterContext().getDpuContext().getPipelineId();
        long activityID = ctx.getExecMasterContext().getDpuContext().getPipelineExecutionId();

        Provenance provContent = new Provenance();
        Context provContext = new Context();
        provContext.setActivityID(activityID + "");
        provContext.setWorkflowID(workflowID + "");
        provContext.setStepID(stepID + "");

        Agent provAgent = new Agent();
        provAgent.setID("UV");
        provAgent.setRole("ETL");

        Activity provAct = new Activity();
        provAct.setTitle("Transform to RDF");
        provAct.setType("StepExecution");

        Communication provCom = new Communication();
        provCom.setRole("transformer");
        provCom.setAgent("RMLService");

        provAct.setCommunication(new ArrayList<Communication>());
        provAct.getCommunication().add(provCom);

        DataProperty provInput = new DataProperty();
        provInput.setKey("harvestedContent");
        DataProperty provOutput = new DataProperty();
        provOutput.setKey("transformerData");

        provContent.setContext(provContext);
        provContent.setAgent(provAgent);
        provContent.setActivity(provAct);
        provContent.setInput(new ArrayList());
        provContent.getInput().add(provInput);
        provContent.setOutput(new ArrayList());
        provContent.getOutput().add(provOutput);
        return provContent;
    }
}