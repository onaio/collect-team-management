package io.ona.collect.android.team.persistence.carriers;

/**
 * Created by Jason Rogena - jrogena@ona.io on 07/08/2017.
 */

public class OdkForm {
    private String displayName;
    private String description;
    private String jrFormId;
    private String jrVersion;
    private String jrDownloadUrl;
    private String jrManifestUrl;
    private String formFilePath;
    private String submissionUri;
    private String base64RSAPublicKey;
    private String displaySubtext;
    private String md5Hash;
    private Long date;
    private String jrCacheFilePath;
    private String formMediaPath;
    private String language;


    public OdkForm() {
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getJrFormId() {
        return jrFormId;
    }

    public void setJrFormId(String jrFormId) {
        this.jrFormId = jrFormId;
    }

    public String getJrVersion() {
        return jrVersion;
    }

    public void setJrVersion(String jrVersion) {
        this.jrVersion = jrVersion;
    }

    public String getJrDownloadUrl() {
        return jrDownloadUrl;
    }

    public void setJrDownloadUrl(String jrDownloadUrl) {
        this.jrDownloadUrl = jrDownloadUrl;
    }

    public String getJrManifestUrl() {
        return jrManifestUrl;
    }

    public void setJrManifestUrl(String jrManifestUrl) {
        this.jrManifestUrl = jrManifestUrl;
    }

    public String getFormFilePath() {
        return formFilePath;
    }

    public void setFormFilePath(String formFilePath) {
        this.formFilePath = formFilePath;
    }

    public String getSubmissionUri() {
        return submissionUri;
    }

    public void setSubmissionUri(String submissionUri) {
        this.submissionUri = submissionUri;
    }

    public String getBase64RSAPublicKey() {
        return base64RSAPublicKey;
    }

    public void setBase64RSAPublicKey(String base64RSAPublicKey) {
        this.base64RSAPublicKey = base64RSAPublicKey;
    }

    public String getDisplaySubtext() {
        return displaySubtext;
    }

    public void setDisplaySubtext(String displaySubtext) {
        this.displaySubtext = displaySubtext;
    }

    public String getMd5Hash() {
        return md5Hash;
    }

    public void setMd5Hash(String md5Hash) {
        this.md5Hash = md5Hash;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public String getJrCacheFilePath() {
        return jrCacheFilePath;
    }

    public void setJrCacheFilePath(String jrCacheFilePath) {
        this.jrCacheFilePath = jrCacheFilePath;
    }

    public String getFormMediaPath() {
        return formMediaPath;
    }

    public void setFormMediaPath(String formMediaPath) {
        this.formMediaPath = formMediaPath;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
