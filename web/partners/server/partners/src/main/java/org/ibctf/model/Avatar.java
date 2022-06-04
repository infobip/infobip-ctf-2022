package org.ibctf.model;

import javax.persistence.*;

@Entity
@Table(name = "avatar")
public class Avatar {

    @Id
    @GeneratedValue
    @Column(name = "avatar_id")
    private Long id;

    @Lob
    @Column(name = "image")
    private String image;

    @Column(name = "checksum")
    private String checksum;

    @Column(name = "file_type")
    private String fileType;

    @OneToOne
    @MapsId
    @JoinColumn(name = "partner_id")
    private Partner partner;

    public Avatar() {
    }

    public Avatar(String image, String checksum, String fileType, Partner partner) {
        this.image = image;
        this.checksum = checksum;
        this.fileType = fileType;
        this.partner = partner;
    }

    public Avatar(Long id, String image, String checksum, String fileType, Partner partner) {
        this.id = id;
        this.image = image;
        this.checksum = checksum;
        this.fileType = fileType;
        this.partner = partner;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Partner getPartner() {
        return partner;
    }

    public void setPartner(Partner partner) {
        this.partner = partner;
    }
}
