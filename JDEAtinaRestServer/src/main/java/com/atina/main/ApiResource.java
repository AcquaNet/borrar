/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.atina.main;
 
import com.atina.cliente.connector.JDEAtinaConnector; 
import com.atina.cliente.exception.ConnectionException;
import com.atina.exception.CustomException;
import com.atina.model.LoginRequest;
import com.atina.model.LoginResponse;
import com.atina.model.LogoutResponse;
import com.atina.service.GRPCConnection;
import com.atina.service.ConnectionPool; 
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.runtime.configuration.ProfileManager; 
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.ws.rs.Consumes; 
import javax.ws.rs.GET; 
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces; 
import javax.ws.rs.core.MediaType; 
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.License;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.annotations.jaxrs.HeaderParam; 
  
/**
 *
 * @author jgodi
 */
@OpenAPIDefinition(
    info = @Info(
        title="Atina JD Rest API",
        version = "1.0.0",
        contact = @Contact(
            name = "Example API Support",
            url = "http://atina.com/contact",
            email = "techsupport@atina.com"),
        license = @License(
            name = "Apache 2.0",
            url = "https://www.apache.org/licenses/LICENSE-2.0.html"))
)
@Path("/")
public class ApiResource {
    
    @ConfigProperty(name = "SERVIDOR_NAME") 
    String servidorName;
    
    @ConfigProperty(name = "SERVIDOR_PORT") 
    Integer servidorPort;
    
    @ConfigProperty(name = "AMBIENTE") 
    String ambiente;
      
    @ConfigProperty(name = "PRODUCT_NAME") 
    String productName;
      
    
    @CheckedTemplate(requireTypeSafeExpressions = false) 
    public static class Templates { 
        public static native TemplateInstance index();
    }
    
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String getIndex() {
    
        String activeProfile = ProfileManager.getActiveProfile();
          
        String alfa = Templates.index()
                .data("activeProfile", activeProfile)
                .data("ambiente",ambiente)
                .data("productName",productName.toUpperCase())
                .render(); 
          
        return alfa;
    }
    
    @POST
    @Path("/login")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Tag(name = "Login", description = "Authenticate in JDE")
            @APIResponses(
            value = {
                    @APIResponse(
                            responseCode = "200",
                            description = "User has been logged",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(type = SchemaType.OBJECT, implementation = LoginResponse.class)))
            }
    ) 
    public Response login(@HeaderParam("Token") String token, @HeaderParam("ChannelId") String channelId ,LoginRequest loginRequest) {
     
        int channelIdValue = 0;
        
        try
        {
              
            JDEAtinaConnector connector;
            
            if(channelId == null || channelId.isEmpty() || channelId.equals("0"))
            {
                
                channelIdValue = ConnectionPool.getInstance().getAvailableChannel();
                
                connector = ConnectionPool.getInstance().createConnectorChannel(
                                                    servidorName, 
                                                    servidorPort, 
                                                    channelIdValue);
                
            } else
            {
                channelIdValue = Integer.parseInt(channelId);
                
                connector = ConnectionPool.getInstance().getConnectorChannel(channelIdValue);
            
            }
            
             
                
            Map<String, Object> entityData = new HashMap<String, Object>();

            entityData.put("Transaction ID", loginRequest.getTransactionId());
            entityData.put("JDE Token", token==null?"":token);
            entityData.put("JDE User", loginRequest.getUser());
            entityData.put("JDE Password", loginRequest.getPassword());
            entityData.put("JDE Environment", loginRequest.getEnvironment());
            entityData.put("JDE Role", loginRequest.getRole());
            entityData.put("Session Id", Integer.toString(0));
           
            Map<String,Object> auth = (Map<String,Object>) connector.authenticate("FromUserData", entityData);
            
            LoginResponse response = new LoginResponse((String)  auth.get("userAddressBookNo"), ((Long) auth.get("sessionId")).intValue());
            
            return Response.ok(response).header("Token", (String)  auth.get("token")).header("ChannelId",  Integer.toString(channelIdValue)).build();
            
        } catch (ConnectionException | NumberFormatException ex)
        {
            throw new CustomException(ex.getMessage(), ex,Integer.toString(channelIdValue)); 
            
        }  catch (Exception  ex)
        {
            throw new CustomException(ex.getMessage(), ex,Integer.toString(channelIdValue)); 
            
        }  
        
    }
     
    @POST
    @Path("/logout")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Tag(name = "Logout", description = "Disconnect From JDE")
            @APIResponses(
            value = {
                    @APIResponse(
                            responseCode = "200",
                            description = "User has been logout",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(type = SchemaType.OBJECT, implementation = LogoutResponse.class)))
            }
    )
    public Response logout(@HeaderParam("Token") String token,  @HeaderParam("ChannelId") String channelId , com.atina.model.LogoutRequest logoutRequest) {
         
        int channelIdValue = 0;
        
        try {
 
            JDEAtinaConnector connector;
            
            if(channelId == null || channelId.isEmpty() || channelId.equals("0"))
            {
                channelIdValue = ConnectionPool.getInstance().getAvailableChannel();
                
                connector = ConnectionPool.getInstance().createConnectorChannel(
                                                    servidorName, 
                                                    servidorPort, 
                                                    channelIdValue);
                
            } else
            {
                channelIdValue = Integer.parseInt(channelId);
                
                connector = ConnectionPool.getInstance().getConnectorChannel(channelIdValue);
            
            }

            Map<String, Object> entityData = new HashMap<String, Object>();

            entityData.put("Transaction ID", logoutRequest.getTransactionId());
            entityData.put("JDE Token", token);

            Map<String, Object> auth = (Map<String, Object>) connector.authenticate("LogoutTokenData", entityData);

            ConnectionPool.getInstance().removeConnectorChannel(channelIdValue);

            LogoutResponse response = new LogoutResponse((String) auth.get("token"), 0);

            return Response.ok(response).header("Token", auth.get("token")).header("ChannelId",  "").build();
            
        } catch (ConnectionException | NumberFormatException ex) {
            
            throw new CustomException(ex.getMessage(), ex, Integer.toString(channelIdValue));

        } catch (Exception ex) {
            
            throw new CustomException(ex.getMessage(), ex, Integer.toString(channelIdValue));

        }
    }
         
}
