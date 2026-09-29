package co.edu.unbosque.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Cargo de consumo de alimentos y bebidas contra la acción de un socio.
 *
 * <p>Es el hecho de negocio central del sistema: la razón por la que existe la trazabilidad. Cada
 * fila describe una cuenta cerrada en un ambiente del club, con su desglose monetario, quién la
 * atendió y cuándo se abrió y cerró.
 *
 * <p>De un consumo cuelgan sus líneas de detalle ({@link ConsumptionItem}), que sostienen toda la
 * analítica de productos, y los avisos generados al socio ({@link Notification}).
 *
 * <h2>El total no se almacena</h2>
 *
 * <p>No hay columna de total. Se recalcula siempre como
 * {@code consumptionValue + iva + service + tip}, tratando los nulos como cero. Esa fórmula está
 * reimplementada de forma independiente en siete lugares del código, de modo que cambiar la regla
 * —por ejemplo, excluir la propina— exige encontrarlos todos.
 *
 * <h2>Ausencia de restricciones</h2>
 *
 * <p>Ningún campo monetario ni temporal es {@code nullable = false}, así que la base admite nulos en
 * todos ellos; por eso cada consumidor se defiende con un método auxiliar que convierte nulo en
 * cero. Tampoco hay validación cruzada en ninguna parte: nada comprueba que el cierre sea posterior
 * a la apertura, ni que la suma de las líneas coincida con {@code consumptionValue}.
 *
 * <h2>Serialización</h2>
 *
 * <p>La entidad se devuelve directamente al cliente. Como {@link #partner}, {@link #items} y
 * {@link #notifications} llevan {@code @JsonIgnore}, el cliente <strong>no ve las líneas del
 * consumo</strong> —ni siquiera en la respuesta del endpoint que acaba de crearlas— y conoce al
 * propietario únicamente a través de {@link #getShareNumber()}.
 *
 * @see co.edu.unbosque.service.PartnerConsumptionService#register(co.edu.unbosque.dto.ConsumptionCreateRequest)
 */
@Entity
@Table(name="partner_consumption")
public class PartnerConsumption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long consumptionId;

    /**
     * Ambiente del club donde se produjo el consumo (restaurante, bar, terraza…).
     *
     * <p><strong>El nombre está escrito sin la segunda «n», y la errata es funcionalmente
     * significativa:</strong> {@code enviroment} es el nombre de propiedad que usan todas las
     * consultas JPQL ({@code c.enviroment}) y se propagó al modelo del frontend. Los DTO de métricas,
     * en cambio, usan la forma correcta {@code environment}. Corregir la grafía aquí rompería las
     * consultas y el contrato con el cliente.
     *
     * <p>No hay catálogo ni enumeración de ambientes: es texto libre. El frontend mantiene una lista
     * fija de diez nombres para los filtros del tablero, sin ninguna relación con este campo, de modo
     * que una errata al registrar produce un ambiente nuevo que nadie encuentra.
     */
    private String enviroment;
    private Integer account;

    /**
     * Mesa en la que se atendió la cuenta.
     *
     * <p>Se mapea a la columna {@code table_number} porque {@code table} es palabra reservada en SQL.
     */
    @Column(name="table_number")
    private String table;

    private String waiterName;
    private Character isPartner;

    /** Valor neto del consumo, sin impuestos ni recargos. Admite nulo. */
    private Double consumptionValue;
    /** Impuesto al valor agregado. Admite nulo. */
    private Double iva;
    /** Recargo por servicio. Admite nulo. */
    private Double service;
    /**
     * Propina. Admite nulo.
     *
     * <p>Nótese que {@code ConsumptionSummaryDTO.tipPercentage} la calcula como porcentaje del
     * consumo neto, no del total facturado.
     */
    private Double tip;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime consumptionOpening;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime consumptionClosing;

    /**
     * Socio al que se carga el consumo. Lado propietario de la clave ajena.
     *
     * <p>La columna {@code person_id} <strong>admite nulos</strong>, y eso tiene un efecto observable
     * en la analítica: las consultas JPQL que referencian {@code c.partner.personId} generan un
     * <em>inner join</em> implícito y excluyen esas filas, mientras que las que solo suman importes
     * las incluyen. Dos métricas del mismo periodo pueden por tanto discrepar.
     *
     * <p>Al ser {@code @JsonIgnore}, el cliente accede al propietario únicamente por
     * {@link #getShareNumber()}.
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="person_id")
    private PersonPartner partner;

    @JsonIgnore
    @OneToMany(
        mappedBy = "consumption",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    private List<Notification> notifications = new ArrayList<>();

    @JsonIgnore
    @OneToMany(
        mappedBy = "consumption",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    private List<ConsumptionItem> items = new ArrayList<>();

    /**
     * Constructor sin argumentos requerido por el proveedor de persistencia.
     *
     * <p>No está pensado para usarse desde el código de la aplicación.
     */
    public PartnerConsumption() {}

    /**
     * Añade un aviso manteniendo sincronizados los dos extremos de la asociación.
     *
     * <p>Nótese que {@link co.edu.unbosque.service.PartnerConsumptionService#register} <strong>no
     * usa este método</strong>: fija el consumo en la notificación y la persiste por el repositorio,
     * de modo que la colección en memoria del padre queda desactualizada dentro de esa transacción.
     *
     * @param n aviso a asociar a este consumo
     */
    public void addNotification(Notification n){
        notifications.add(n);
        n.setConsumption(this);
    }

    /**
     * Desasocia un aviso de este consumo.
     *
     * <p>Al haber {@code orphanRemoval = true}, poner el consumo a {@code null} <strong>elimina la
     * fila</strong> de la base al sincronizar, no solo la desvincula.
     *
     * <p>Actualmente ningún código invoca este método.
     *
     * @param n aviso a desasociar y eliminar
     */
    public void removeNotification(Notification n){
        notifications.remove(n);
        n.setConsumption(null);
    }

    /**
     * Añade una línea de detalle manteniendo sincronizados los dos extremos de la asociación.
     *
     * <p>Es la vía por la que {@link co.edu.unbosque.service.PartnerConsumptionService#register}
     * adjunta las líneas antes de guardar: al tener la asociación {@code cascade = ALL}, el guardado
     * del consumo las persiste en la misma operación.
     *
     * <p>No existe el método inverso {@code removeItem}.
     *
     * @param item línea de detalle a asociar a este consumo
     */
    public void addItem(ConsumptionItem item){
        items.add(item);
        item.setConsumption(this);
    }

    /**
     * Devuelve las líneas de detalle del consumo.
     *
     * @return las líneas de detalle del consumo
     */
    public List<ConsumptionItem> getItems() { return items; }
    /**
     * Establece las líneas de detalle del consumo.
     *
     * @param items las líneas de detalle del consumo
     */
    public void setItems(List<ConsumptionItem> items) { this.items = items; }

	/**
	 * Devuelve el identificador del consumo.
	 *
	 * @return el identificador del consumo
	 */
	public Long getConsumptionId() {
		return consumptionId;
	}

	/**
	 * Establece el identificador del consumo.
	 *
	 * @param consumptionId el identificador del consumo
	 */
	public void setConsumptionId(Long consumptionId) {
		this.consumptionId = consumptionId;
	}

	/**
	 * Devuelve el ambiente del club donde se produjo el consumo.
	 *
	 * @return el ambiente del club donde se produjo el consumo
	 */
	public String getEnviroment() {
		return enviroment;
	}

	/**
	 * Establece el ambiente del club donde se produjo el consumo.
	 *
	 * @param enviroment el ambiente del club donde se produjo el consumo
	 */
	public void setEnviroment(String enviroment) {
		this.enviroment = enviroment;
	}

	/**
	 * Devuelve el número de cuenta del consumo.
	 *
	 * @return el número de cuenta del consumo
	 */
	public Integer getAccount() {
		return account;
	}

	/**
	 * Establece el número de cuenta del consumo.
	 *
	 * @param account el número de cuenta del consumo
	 */
	public void setAccount(Integer account) {
		this.account = account;
	}

	/**
	 * Devuelve la mesa en la que se atendio la cuenta.
	 *
	 * @return la mesa en la que se atendio la cuenta
	 */
	public String getTable() {
		return table;
	}

	/**
	 * Establece la mesa en la que se atendio la cuenta.
	 *
	 * @param table la mesa en la que se atendio la cuenta
	 */
	public void setTable(String table) {
		this.table = table;
	}

	/**
	 * Devuelve el nombre del mesero que atendio.
	 *
	 * @return el nombre del mesero que atendio
	 */
	public String getWaiterName() {
		return waiterName;
	}

	/**
	 * Establece el nombre del mesero que atendio.
	 *
	 * @param waiterName el nombre del mesero que atendio
	 */
	public void setWaiterName(String waiterName) {
		this.waiterName = waiterName;
	}

	/**
	 * Devuelve el indicador de si el consumo corresponde a un socio.
	 *
	 * @return el indicador de si el consumo corresponde a un socio
	 */
	public Character getIsPartner() {
		return isPartner;
	}

	/**
	 * Establece el indicador de si el consumo corresponde a un socio.
	 *
	 * @param isPartner el indicador de si el consumo corresponde a un socio
	 */
	public void setIsPartner(Character isPartner) {
		this.isPartner = isPartner;
	}

	/**
	 * Devuelve el valor neto del consumo, sin impuestos ni recargos.
	 *
	 * @return el valor neto del consumo, sin impuestos ni recargos
	 */
	public Double getConsumptionValue() {
		return consumptionValue;
	}

	/**
	 * Establece el valor neto del consumo, sin impuestos ni recargos.
	 *
	 * @param consumptionValue el valor neto del consumo, sin impuestos ni recargos
	 */
	public void setConsumptionValue(Double consumptionValue) {
		this.consumptionValue = consumptionValue;
	}

	/**
	 * Devuelve el impuesto al valor agregado.
	 *
	 * @return el impuesto al valor agregado
	 */
	public Double getIva() {
		return iva;
	}

	/**
	 * Establece el impuesto al valor agregado.
	 *
	 * @param iva el impuesto al valor agregado
	 */
	public void setIva(Double iva) {
		this.iva = iva;
	}

	/**
	 * Devuelve el recargo por servicio.
	 *
	 * @return el recargo por servicio
	 */
	public Double getService() {
		return service;
	}

	/**
	 * Establece el recargo por servicio.
	 *
	 * @param service el recargo por servicio
	 */
	public void setService(Double service) {
		this.service = service;
	}

	/**
	 * Devuelve la propina.
	 *
	 * @return la propina
	 */
	public Double getTip() {
		return tip;
	}

	/**
	 * Establece la propina.
	 *
	 * @param tip la propina
	 */
	public void setTip(Double tip) {
		this.tip = tip;
	}

	/**
	 * Devuelve el momento de apertura del consumo.
	 *
	 * @return el momento de apertura del consumo
	 */
	public LocalDateTime getConsumptionOpening() {
		return consumptionOpening;
	}

	/**
	 * Establece el momento de apertura del consumo.
	 *
	 * @param consumptionOpening el momento de apertura del consumo
	 */
	public void setConsumptionOpening(LocalDateTime consumptionOpening) {
		this.consumptionOpening = consumptionOpening;
	}

	/**
	 * Devuelve el momento de cierre del consumo.
	 *
	 * @return el momento de cierre del consumo
	 */
	public LocalDateTime getConsumptionClosing() {
		return consumptionClosing;
	}

	/**
	 * Establece el momento de cierre del consumo.
	 *
	 * @param consumptionClosing el momento de cierre del consumo
	 */
	public void setConsumptionClosing(LocalDateTime consumptionClosing) {
		this.consumptionClosing = consumptionClosing;
	}

	/**
	 * Devuelve el socio asociado.
	 *
	 * @return el socio asociado
	 */
	public PersonPartner getPartner() {
		return partner;
	}

	/**
	 * Establece el socio asociado.
	 *
	 * @param partner el socio asociado
	 */
	public void setPartner(PersonPartner partner) {
		this.partner = partner;
	}

	/**
	 * Número de acción del socio propietario, expuesto al cliente como campo {@code shareNumber}.
	 *
	 * <p>No es una columna: es un valor derivado. Existe porque {@link #partner} es
	 * {@code @JsonIgnore}, de modo que este getter es <strong>la única vía por la que el cliente
	 * conoce al propietario del consumo</strong>.
	 *
	 * <p><strong>Consideración técnica importante.</strong> Desreferencia un proxy {@code LAZY}
	 * durante la serialización de Jackson, es decir <em>después</em> de que la transacción del
	 * servicio haya terminado. Eso funciona únicamente porque {@code spring.jpa.open-in-view} no está
	 * configurado y conserva su valor por defecto {@code true}. Desactivarlo —el endurecimiento
	 * habitual en producción— convertiría en {@code LazyInitializationException} los cuatro endpoints
	 * que devuelven listas de consumos.
	 *
	 * <p>Incluso funcionando, provoca un {@code SELECT} adicional por cada fila de cada página.
	 *
	 * @return el número de acción del socio, o {@code null} si el consumo no tiene socio asociado
	 */
	@jakarta.persistence.Transient
	@com.fasterxml.jackson.annotation.JsonProperty("shareNumber")
	public Long getShareNumber() {
		return partner != null ? partner.getShareNumber() : null;
	}

	/**
	 * Devuelve los avisos del consumo.
	 *
	 * @return los avisos del consumo
	 */
	public List<Notification> getNotifications() {
		return notifications;
	}

	/**
	 * Establece los avisos del consumo.
	 *
	 * @param notifications los avisos del consumo
	 */
	public void setNotifications(List<Notification> notifications) {
		this.notifications = notifications;
	}

}
