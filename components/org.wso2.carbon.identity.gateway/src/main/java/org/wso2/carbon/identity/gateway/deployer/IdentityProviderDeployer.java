/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.gateway.deployer;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.deployment.engine.Artifact;
import org.wso2.carbon.deployment.engine.ArtifactType;
import org.wso2.carbon.deployment.engine.Deployer;
import org.wso2.carbon.deployment.engine.exception.CarbonDeploymentException;
import org.wso2.carbon.identity.gateway.common.model.idp.IdentityProviderConfig;
import org.wso2.carbon.identity.gateway.common.model.idp.IdentityProviderEntity;
import org.wso2.carbon.identity.gateway.internal.FrameworkServiceComponent;
import org.wso2.carbon.identity.gateway.store.IdentityProviderConfigStore;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class IdentityProviderDeployer implements Deployer {

    private ArtifactType artifactType;
    private URL repository;


    private Logger logger = LoggerFactory.getLogger(FrameworkServiceComponent.class);

    @Override
    public void init() {
        artifactType = new ArtifactType<>("identityprovider");


        try {
            repository = new URL("file:" + Paths.get(System.getProperty("carbon.home", "."), "deployment",
                                                     "identityprovider")
                    .toString
                            ());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String deploy(Artifact artifact) throws CarbonDeploymentException {
        IdentityProviderConfig identityProviderConfig = getIdentityProviderConfig(artifact);
        IdentityProviderConfigStore.getInstance().addIdentityProvider(identityProviderConfig);
        return artifact.getName();
    }

    @Override
    public void undeploy(Object key) throws CarbonDeploymentException {
        if (!(key instanceof String)) {
            throw new CarbonDeploymentException("Error while Un Deploying : " + key + "is not a String value");
        }
        logger.debug("Undeploying : " + key);

    }

    @Override
    public Object update(Artifact artifact) throws CarbonDeploymentException {
        logger.debug("Updating : " + artifact.getName());

        return artifact.getName();
    }

    @Override
    public URL getLocation() {

        logger.debug("Updating : "  );

        return repository;
    }

    @Override
    public ArtifactType getArtifactType() {

        logger.debug("Updating : "  );
        return artifactType;
    }

    /**
     * Read the artifacts and save the policy and metadata to PolicyStore and PolicyCollection
     * @param artifact deployed articles
     */
    private synchronized IdentityProviderConfig getIdentityProviderConfig(Artifact artifact) {
        String artifactName = artifact.getPath();
        IdentityProviderConfig identityProviderConfig = null;
        Path path = Paths.get(artifactName);
        if (Files.exists(path)) {
            try {
                Reader in = new InputStreamReader(Files.newInputStream(path), StandardCharsets.UTF_8);
                Yaml yaml = new Yaml();
                yaml.setBeanAccess(BeanAccess.FIELD);
                IdentityProviderEntity identityProviderEntity = yaml.loadAs(in, IdentityProviderEntity.class);
                if (identityProviderEntity != null) {
                    identityProviderConfig = identityProviderEntity.getIdentityProviderConfig();
                }

            } catch (Exception e) {

            }
        }
        return identityProviderConfig;
    }
}