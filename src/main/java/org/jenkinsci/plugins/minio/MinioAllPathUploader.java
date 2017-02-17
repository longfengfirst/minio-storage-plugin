package org.jenkinsci.plugins.minio;

import io.minio.MinioClient;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidArgumentException;
import io.minio.errors.InvalidBucketNameException;
import io.minio.errors.NoResponseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.jenkinsci.plugins.minio.MinioUploader.DescriptorImpl;
import org.jenkinsci.remoting.RoleChecker;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.xmlpull.v1.XmlPullParserException;

import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import hudson.tasks.Builder;

/**
 * Sample {@link Builder}.
 *
 * <p>
 * When the user calls the act method, the invoke method is called
 * which in turn uploads the files using minioCLinet.
 * 
 * <p>
 * @author Ashish Sinha
 *
 */

public class MinioAllPathUploader implements FileCallable<Void> {
	private static final long serialVersionUID = 1;

	/**
	 * Prefix to be added to the object name while.
	 */
	private transient MinioClient minioClient;

	/**
	 * Bucket name to store the build artifacts.
	 */
	private String bucketName;

	/**
	 * path represents a file path on a specific agent or the master.
	 */
	private FilePath path;

	/**
	 * The name of the file .
	 */
	private String fileName;

	/**
	 * TaskListener listener needed for reading the exception.
	 */
	TaskListener listener;

	@DataBoundConstructor
	public MinioAllPathUploader(MinioClient minioClient, String bucketName,
			FilePath path, String fileName, TaskListener listener) {
		this.bucketName = bucketName;
		this.minioClient = minioClient;
		this.path = path;
		this.fileName = fileName;
		this.listener = listener;
	}

	@Override
	public void checkRoles(RoleChecker checker) throws SecurityException {

	}

	
	/**
	 * This method uses minioClient to upload the Object.
	 * 
	 * @return null
	 */
	@Override
	public Void invoke(File f, VirtualChannel channel) throws IOException,
			InterruptedException {
		long size = path.length();
		InputStream stream = null;

		try {

			stream = new FileInputStream(path.getRemote());
			String contentType = "application/octet-stream";
			minioClient.putObject(bucketName, fileName, stream, size,contentType);

		} catch (InvalidKeyException | InvalidBucketNameException
				| NoSuchAlgorithmException | InsufficientDataException
				| NoResponseException | ErrorResponseException
				| InternalException | InvalidArgumentException
				| XmlPullParserException e) {
			e.printStackTrace(listener
					.error("Minio error, failed to upload files"));
			// run.setResult(Result.UNSTABLE);
		} catch (IOException e) {
			e.printStackTrace(listener
					.error("Communication error, failed to upload files"));
			// run.setResult(Result.UNSTABLE);
		} finally {
			if (stream != null)
				stream.close();
		}
		return null;

	}

}
