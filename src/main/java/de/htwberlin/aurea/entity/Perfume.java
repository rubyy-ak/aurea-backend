package de.htwberlin.aurea.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** Ein Parfüm in der Sammlung oder auf der Wunschliste eines Nutzers. */
@Entity
@Table(name = "perfume")
public class Perfume {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "owner_id")
	private AppUser owner;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(nullable = false, length = 100)
	private String brand;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Concentration concentration;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private ScentFamily family;

	@Column(length = 255)
	private String topNotes;

	@Column(length = 255)
	private String heartNotes;

	@Column(length = 255)
	private String baseNotes;

	private Integer sizeMl;

	@Column(precision = 8, scale = 2)
	private BigDecimal priceEur;

	/** 1 bis 5 Sterne, optional. */
	private Integer rating;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private PerfumeStatus status = PerfumeStatus.OWNED;

	@Column(nullable = false)
	private boolean favorite;

	@Column(nullable = false)
	private int wearCount;

	private LocalDate lastWornOn;

	@Column(nullable = false)
	private Instant createdAt = Instant.now();

	protected Perfume() {
	}

	public Perfume(AppUser owner) {
		this.owner = owner;
	}

	/** Protokolliert, dass das Parfüm am angegebenen Tag getragen wurde. */
	public void wear(LocalDate day) {
		wearCount++;
		if (lastWornOn == null || day.isAfter(lastWornOn)) {
			lastWornOn = day;
		}
	}

	public Long getId() { return id; }
	public AppUser getOwner() { return owner; }

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public String getBrand() { return brand; }
	public void setBrand(String brand) { this.brand = brand; }

	public Concentration getConcentration() { return concentration; }
	public void setConcentration(Concentration concentration) { this.concentration = concentration; }

	public ScentFamily getFamily() { return family; }
	public void setFamily(ScentFamily family) { this.family = family; }

	public String getTopNotes() { return topNotes; }
	public void setTopNotes(String topNotes) { this.topNotes = topNotes; }

	public String getHeartNotes() { return heartNotes; }
	public void setHeartNotes(String heartNotes) { this.heartNotes = heartNotes; }

	public String getBaseNotes() { return baseNotes; }
	public void setBaseNotes(String baseNotes) { this.baseNotes = baseNotes; }

	public Integer getSizeMl() { return sizeMl; }
	public void setSizeMl(Integer sizeMl) { this.sizeMl = sizeMl; }

	public BigDecimal getPriceEur() { return priceEur; }
	public void setPriceEur(BigDecimal priceEur) { this.priceEur = priceEur; }

	public Integer getRating() { return rating; }
	public void setRating(Integer rating) { this.rating = rating; }

	public PerfumeStatus getStatus() { return status; }
	public void setStatus(PerfumeStatus status) { this.status = status; }

	public boolean isFavorite() { return favorite; }
	public void setFavorite(boolean favorite) { this.favorite = favorite; }

	public int getWearCount() { return wearCount; }
	public LocalDate getLastWornOn() { return lastWornOn; }
	public Instant getCreatedAt() { return createdAt; }
}
